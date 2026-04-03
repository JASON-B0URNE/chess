package server;

import chess.ChessGame;
import com.google.gson.Gson;
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

    public Server() {
        var serializer = new Gson();

        AuthService authService = new AuthService();
        UserService userService = new UserService();
        GameService gameService = new GameService();

        Map<Integer, Set<WsContext>> gameSessions = new ConcurrentHashMap<>();

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
                UserGameCommand message = serializer.fromJson(ctx.message(), UserGameCommand.class);

                AuthData user = authService.getUser(message);
                GameData gameData = gameService.getGame(message);

                if (user == null || gameData == null) {
                    ErrorMessage errorMessage = new ErrorMessage("Error: Invalid user command.");

                    ctx.send(serializer.toJson(errorMessage));
                    return;
                }

                Integer gameID = gameData.gameID();

                gameSessions.putIfAbsent(gameID, ConcurrentHashMap.newKeySet());
                gameSessions.get(gameID).add(ctx);

                if (Objects.equals(message.getCommandType(), UserGameCommand.CommandType.CONNECT)) {
                    LoadGameMessage loadMessage = new LoadGameMessage(gameData.game());

                    NotificationMessage notificationMessage;

                    if (Objects.equals(gameData.whiteUsername(), user.username())) {
                        notificationMessage = new NotificationMessage("Player " + user.username() + " joined as WHITE\n");
                    } else if (Objects.equals(gameData.blackUsername(), user.username())) {
                        notificationMessage = new NotificationMessage("Player " + user.username() + " joined as BLACK\n");
                    } else {
                        notificationMessage = new NotificationMessage("Player " + user.username() + " joined as an OBSERVER\n");
                    }

                    for (WsContext client : gameSessions.get(gameID)) {
                        try {
                            if (!client.session.isOpen()) {
                                gameSessions.get(gameID).remove(client);
                            }

                            if (client != ctx) {
                                client.send(serializer.toJson(notificationMessage));
                            } else {
                                client.send(serializer.toJson(loadMessage));
                            }
                        } catch (Exception e) {
                            gameSessions.get(gameID).remove(client);
                        }
                    }
                } else if (Objects.equals(message.getCommandType(), UserGameCommand.CommandType.MAKE_MOVE)) {
                    LoadGameMessage loadMessage = new LoadGameMessage(gameData.game());
                    System.out.println("Server Message: " + ctx.message());

                    NotificationMessage notificationMessage;

                    if (Objects.equals(gameData.whiteUsername(), user.username())) {
                        notificationMessage = new NotificationMessage("Player " + user.username() + " joined as WHITE\n");
                    } else if (Objects.equals(gameData.blackUsername(), user.username())) {
                        notificationMessage = new NotificationMessage("Player " + user.username() + " joined as BLACK\n");
                    } else {
                        notificationMessage = new NotificationMessage("Player " + user.username() + " joined as an OBSERVER\n");
                    }

                    for (WsContext client : gameSessions.get(gameID)) {
                        try {
                            if (!client.session.isOpen()) {
                                gameSessions.get(gameID).remove(client);
                            }

                            if (client != ctx) {
                                client.send(serializer.toJson(notificationMessage));
                            } else {
                                client.send(serializer.toJson(loadMessage));
                            }
                        } catch (Exception e) {
                            gameSessions.get(gameID).remove(client);
                        }
                    }
                }
            });
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
