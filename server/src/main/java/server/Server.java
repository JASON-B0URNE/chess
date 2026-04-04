package server;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import dataaccess.GameDOA;
import io.javalin.*;
import io.javalin.websocket.WsContext;
import model.AuthData;
import model.GameData;
import model.UserData;
import requests.CreateGame;
import requests.JoinGame;
import requests.Response;
import services.AuthService;
import services.GameService;
import services.UserService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static dataaccess.DatabaseManager.createDatabase;
import static dataaccess.DatabaseManager.createTables;

public class Server {
    private final Javalin javalin;
    private AuthService authService = new AuthService();
    private UserService userService = new UserService();
    private GameService gameService = new GameService();
    private Map<Integer, Set<WsContext>> gameSessions = new ConcurrentHashMap<>();
    private Map<Integer, String> gameStatus = new ConcurrentHashMap<>();

    public Server() {
        var serializer = new Gson();

        try {
            createDatabase();
        } catch (SQLException ex) {
            System.out.print("Create DB Failed");
        }
        try {
            createTables();
        } catch (SQLException ex) {
            System.out.print("Create Table Failed");
        }

        javalin = Javalin.create(config -> {config.staticFiles.add("web");})
            .delete("/db", ctx -> {
                Response response = authService.clear();
                ctx.status(response.code()).result(response.json());
            })
            .post("/user", ctx -> {
                UserData newUser = serializer.fromJson(ctx.body(), UserData.class);
                Response response = userService.createUser(newUser);
                ctx.status(response.code()).result(response.json());
            })
            .post("/session", ctx -> {
                UserData newUser = serializer.fromJson(ctx.body(), UserData.class);
                Response response = authService.createSession(newUser);
                ctx.status(response.code()).result(response.json());
            })
            .delete("/session", ctx -> {
                String authToken = ctx.header("Authorization");
                Response response = authService.deleteSession(authToken);
                ctx.status(response.code()).result(response.json());
            })
            .get("/game", ctx -> {
                String authToken = ctx.header("Authorization");

                Response response = gameService.getGames(authToken);
                ctx.status(response.code()).result(response.json());
            })
            .post("/game", ctx -> {
                String authToken = ctx.header("Authorization");
                GameData newGame = serializer.fromJson(ctx.body(), GameData.class);

                Response response = gameService.createGame(new CreateGame(authToken, newGame));
                ctx.status(response.code()).result(response.json());
            })
            .put("/game", ctx -> {
                String authToken = ctx.header("Authorization");
                JoinGame request = serializer.fromJson(ctx.body(), JoinGame.class);

                Response response = gameService.joinGame(new JoinGame(authToken, request.playerColor(), request.gameID()));
                ctx.status(response.code()).result(response.json());
            }
        );

        javalin.ws("/ws", ws -> {
            ws.onMessage(ctx -> {
                websocket(ctx, ctx.message());
            });
        });
    }

    private void websocket(WsContext ctx, String command) throws SQLException {
        var serializer = new Gson();
        UserGameCommand message = serializer.fromJson(command, UserGameCommand.class);

        AuthData user = authService.getUser(message);
        GameData gameData = gameService.getGame(message);

        if (user == null || gameData == null) {
            ErrorMessage errorMessage = new ErrorMessage("Error: Invalid user command.\n");
            ctx.send(serializer.toJson(errorMessage));

            return;
        }

        Integer gameID = gameData.gameID();

        if (gameSessions.get(gameID) != null && !gameSessions.get(gameID).isEmpty()) {
            for (WsContext client : gameSessions.get(gameID)) {
                if (!client.session.isOpen()) {
                    gameSessions.get(gameID).remove(client);
                }
            }
            if (gameSessions.get(gameID).isEmpty()) {
                gameStatus.remove(gameID);
            }
        }

        gameSessions.putIfAbsent(gameID, ConcurrentHashMap.newKeySet());
        gameSessions.get(gameID).add(ctx);
        gameStatus.putIfAbsent(gameID, "NORMAL");

        ChessGame game = gameData.game();

        if (Objects.equals(message.getCommandType(), UserGameCommand.CommandType.CONNECT)) {
            connect(game, gameData, user, ctx, gameID);
        } else if (Objects.equals(message.getCommandType(), UserGameCommand.CommandType.MAKE_MOVE)) {
            makeMove(command, game, gameData, user, ctx, gameID);
        } else if (Objects.equals(message.getCommandType(), UserGameCommand.CommandType.RESIGN)) {
            if (!Objects.equals(gameStatus.get(gameID), "NORMAL")) {
                ErrorMessage errorMessage = new ErrorMessage("Error: Game is not in session.\n");
                ctx.send(serializer.toJson(errorMessage));

                return;
            }

            if (!Objects.equals(user.username(), gameData.blackUsername()) &&
                    !Objects.equals(user.username(), gameData.whiteUsername())) {
                ErrorMessage errorMessage = new ErrorMessage("Error: Invalid command.\n");
                ctx.send(serializer.toJson(errorMessage));

                return;
            }

            NotificationMessage notificationMessage = null;
            notificationMessage = new NotificationMessage("Player " + user.username() + " has resigned.\n");

            gameStatus.put(gameID, "RESIGN");

            for (WsContext client : gameSessions.get(gameID)) {
                try {
                    client.send(serializer.toJson(notificationMessage));

                } catch (Exception e) {
                    gameSessions.get(gameID).remove(client);
                }
            }
        } else if (Objects.equals(message.getCommandType(), UserGameCommand.CommandType.LEAVE)) {
            NotificationMessage notificationMessage = new NotificationMessage("Player " + user.username() + " has left the game.\n");
            gameSessions.get(gameID).remove(ctx);

            GameDOA gameDOA = new GameDOA();

            if (Objects.equals(gameData.whiteUsername(), user.username())) {
                GameData updateData = new GameData(gameID, null, gameData.blackUsername(),
                        gameData.gameName(), game);
                gameDOA.replace(updateData);
            } else if (Objects.equals(gameData.blackUsername(), user.username())) {
                GameData updateData = new GameData(gameID, gameData.whiteUsername(), null,
                        gameData.gameName(), game);
                gameDOA.replace(updateData);
            }

            for (WsContext client : gameSessions.get(gameID)) {
                try {
                    client.send(serializer.toJson(notificationMessage));

                } catch (Exception e) {
                    gameSessions.get(gameID).remove(client);
                }
            }
        }
    }

    private void makeMove(String command, ChessGame game, GameData gameData,
            AuthData user, WsContext ctx, Integer gameID) throws SQLException {
        var serializer = new Gson();

        MakeMoveCommand makeMove = serializer.fromJson(command, MakeMoveCommand.class);
        ChessMove move = makeMove.getMove();
        ChessPosition startPosition = new ChessPosition(move.getStartPosition().getRow(), move.getStartPosition().getColumn());
        ChessPosition endPosition = new ChessPosition(move.getEndPosition().getRow(), move.getEndPosition().getColumn());

        NotificationMessage notificationMessage;

        move = new ChessMove(startPosition, endPosition, move.getPromotionPiece());
        ChessBoard board = game.getBoard();

        ChessGame.TeamColor currentColor = game.getTeamTurn();

        ChessGame.TeamColor oppositeColor;
        String oppositeUsername;

        if (currentColor == ChessGame.TeamColor.BLACK) {
            oppositeColor = ChessGame.TeamColor.WHITE;
            oppositeUsername = gameData.whiteUsername();
        } else {
            oppositeColor = ChessGame.TeamColor.BLACK;
            oppositeUsername = gameData.blackUsername();
        }

        if (Objects.equals(user.username(), oppositeUsername)) {
            ErrorMessage errorMessage = new ErrorMessage("Error: Invalid move.\n");
            ctx.send(serializer.toJson(errorMessage));

            return;
        }

        try {
            if (!Objects.equals(gameStatus.get(gameID), "NORMAL")) {
                ErrorMessage errorMessage = new ErrorMessage("Error: Game is not in session.\n");
                ctx.send(serializer.toJson(errorMessage));

                return;
            }

            game.makeMove(move);
        } catch (Exception e) {
            ErrorMessage errorMessage = new ErrorMessage("Error: Invalid move.\n");
            ctx.send(serializer.toJson(errorMessage));

            return;
        }

        LoadGameMessage loadMessage = new LoadGameMessage(game);
        GameDOA gameDOA = new GameDOA();
        GameData updateData = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(),
                gameData.gameName(), game);
        gameDOA.replace(updateData);

        NotificationMessage specialMessage = null;

        boolean check = game.isInCheck(oppositeColor);
        if (check) {
            specialMessage = new NotificationMessage(oppositeColor + " Player " + oppositeUsername + " - is in check.\n");

            boolean checkmate = game.isInCheckmate(oppositeColor);
            if (checkmate) {
                specialMessage = new NotificationMessage(oppositeColor + " Player " + oppositeUsername + " - is checkmated.\n");
            }
        }

        boolean stalemate = game.isInStalemate(oppositeColor);
        if (stalemate) {
            specialMessage = new NotificationMessage("Game is in stalemate.\n");
            gameStatus.put(gameID, "STALEMATE");
        }

        if (Objects.equals(gameData.whiteUsername(), user.username())) {
            notificationMessage = new NotificationMessage("WHITE Player " + user.username() + " - moved " +
                    board.getPiece(move.getStartPosition()).toString() +
                    " " + parsePosition(move.getStartPosition()) + " to " + parsePosition(move.getEndPosition()) + ".\n");
        } else if (Objects.equals(gameData.blackUsername(), user.username())) {
            notificationMessage = new NotificationMessage("BLACK Player " + user.username() + " - moved " +
                    board.getPiece(move.getStartPosition()).toString() +
                    " " + parsePosition(move.getStartPosition()) + " to " + parsePosition(move.getEndPosition()) + ".\n");
        } else {
            ErrorMessage errorMessage = new ErrorMessage("Error: Invalid user command.\n");
            ctx.send(serializer.toJson(errorMessage));

            return;
        }

        for (WsContext client : gameSessions.get(gameID)) {
            try {
                if (specialMessage != null) { client.send(serializer.toJson(specialMessage)); }

                if (!Objects.equals(client, ctx)) {
                    client.send(serializer.toJson(notificationMessage));
                    client.send(serializer.toJson(loadMessage));
                } else {
                    client.send(serializer.toJson(loadMessage));
                }

            } catch (Exception e) { gameSessions.get(gameID).remove(client); }
        }
    }

    private void connect(ChessGame game, GameData gameData,
                         AuthData user, WsContext ctx, Integer gameID) {
        var serializer = new Gson();
        LoadGameMessage loadMessage = new LoadGameMessage(game);

        NotificationMessage notificationMessage;

        if (Objects.equals(gameData.whiteUsername(), user.username())) {
            notificationMessage = new NotificationMessage("Player " + user.username() + " joined as WHITE.\n");
        } else if (Objects.equals(gameData.blackUsername(), user.username())) {
            notificationMessage = new NotificationMessage("Player " + user.username() + " joined as BLACK.\n");
        } else {
            notificationMessage = new NotificationMessage("Player " + user.username() + " joined as an OBSERVER.\n");
        }

        for (WsContext client : gameSessions.get(gameID)) {
            try {
                if (!Objects.equals(client, ctx)) {
                    client.send(serializer.toJson(notificationMessage));
                } else {
                    client.send(serializer.toJson(loadMessage));
                }
            } catch (Exception e) {
                gameSessions.get(gameID).remove(client);
            }
        }
    }

    private String parsePosition(ChessPosition position) {
        Integer row = position.getRow();
        Integer col = position.getColumn();

        String string = null;

        if (row == 1) {
            string += "a";
        } else if (row == 2) {
            string += "b";
        } else if (row == 3) {
            string += "c";
        } else if (row == 4) {
            string += "d";
        } else if (row == 5) {
            string += "e";
        } else if (row == 6) {
            string += "f";
        } else if (row == 7) {
            string += "g";
        } else if (row == 8) {
            string += "h";
        }

        string += col.toString();

        return string;
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
