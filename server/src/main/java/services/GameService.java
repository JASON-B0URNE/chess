package services;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AuthDOA;
import dataaccess.GameDOA;
import dataaccess.InterfaceDOA;
import model.AuthData;
import model.GameData;
import requests.CreateGame;
import requests.JoinGame;
import requests.Response;
import websocket.commands.UserGameCommand;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class GameService {
    InterfaceDOA<AuthData> authDOA = new AuthDOA();
    InterfaceDOA<GameData> gameDOA = new GameDOA();

    public GameService() {
    }

    public Response joinGame(JoinGame request) {
        var serializer = new Gson();
        AuthData session;

        try {
            session = authDOA.get(request.authToken());
        } catch (SQLException ex) {
            return new Response(500, serializer.toJson(Map.of("message", "Error: database error")));
        }

        if (session == null) {
            return new Response(401, serializer.toJson(Map.of("message", "Error: unauthorized")));
        }

        if (request.gameID() < 0 || !(Objects.equals(request.playerColor(), "WHITE") || Objects.equals(request.playerColor(), "BLACK"))) {
            return new Response(400, serializer.toJson(Map.of("message", "Error: bad request")));
        }

        Collection<GameData> games;
        try {
            games = gameDOA.list();
        } catch (SQLException ex) {
            return new Response(500, serializer.toJson(Map.of("message", "Error: database error")));
        }

        System.out.println("All Games: " + serializer.toJson(games));

        AtomicReference<GameData> game = new AtomicReference<>();

        games.forEach( x -> {
            if (Objects.equals(x.gameID(), request.gameID())) {
                game.set(x);
            }
        });

        if (game.get() == null) {
            return new Response(400, serializer.toJson(Map.of("message", "Error: unauthorized")));
        }

        if ((game.get().blackUsername() != null && Objects.equals(request.playerColor(), "BLACK")) ||
                (game.get().whiteUsername() != null && Objects.equals(request.playerColor(), "WHITE"))) {
            return new Response(403, serializer.toJson(Map.of("message", "Error: already taken")));
        }

        if (Objects.equals(request.playerColor(), "BLACK")) {
            try {
                gameDOA.replace(new GameData(game.get().gameID(), game.get().whiteUsername(),
                        session.username(), game.get().gameName(), game.get().game()));
            } catch (SQLException ex) {
                return new Response(500, serializer.toJson(Map.of("message", "Error: database error")));
            }
        } else {
            try {
                gameDOA.replace(new GameData(game.get().gameID(), session.username(),
                        game.get().blackUsername(), game.get().gameName(), game.get().game()));
            } catch (SQLException ex) {
                return new Response(500, serializer.toJson(Map.of("message", "Error: database error")));
            }
        }

        return new Response(200, "{}");
    }

    public Response getGames(String authToken) {
        var serializer = new Gson();
        AuthData session;

        try{
            session = authDOA.get(authToken);
        } catch (SQLException ex) {
            return new Response(500, serializer.toJson(Map.of("message", "Error: database error")));
        }
        if (session == null) {
            return new Response(401, serializer.toJson(Map.of("message", "Error: unauthorized")));
        }

        Collection<GameData> games;
        try {
            games = gameDOA.list();
        } catch (SQLException ex) {
            return new Response(500, serializer.toJson(Map.of("message", "Error: database error")));
        }

        System.out.print("Games: " + games);
        return new Response(200, serializer.toJson(Map.of("games", games)));
    }

    public Response createGame(CreateGame request) {
        var serializer = new Gson();
        AuthData session;

        try {
            session = authDOA.get(request.authToken());
        } catch (SQLException ex) {
            return new Response(500, serializer.toJson(Map.of("message", "Error: database error")));
        }

        if (session == null) {
            return new Response(401, serializer.toJson(Map.of("message", "Error: unauthorized")));
        }

        GameData newGame = request.game();

        if (newGame.gameName() == null) {
            return new Response(400, serializer.toJson(Map.of("message", "Error: bad request")));
        }

        GameData oldGame;
        try {
            oldGame = gameDOA.get(null);
        } catch (SQLException ex) {
            return new Response(500, serializer.toJson(Map.of("message", "Error: database error")));
        }

        ChessGame chessGame = new ChessGame();

        int gameID = 1;
        if (oldGame != null) {
            gameID = oldGame.gameID() + 1;
        }
        try {
            gameDOA.create(new GameData(gameID, null, null, newGame.gameName(), chessGame));
        } catch (SQLException ex) {
            return new Response(500, serializer.toJson(Map.of("message", "Error: database error")));
        }
        return new Response(200, serializer.toJson(Map.of("gameID", gameID)));
    }

    public GameData getGame(UserGameCommand command) throws SQLException {
        AuthData session;

        try {
            session = authDOA.get(command.getAuthToken());
        } catch (SQLException ex) {
            return null;
        }

        if (session == null) {
            return null;
        }

        GameData gameData = gameDOA.get(command.getGameID().toString());

        return gameData;
    }
}
