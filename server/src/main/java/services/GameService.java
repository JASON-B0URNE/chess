package services;

import com.google.gson.Gson;
import dataaccess.InterfaceDOA;
import model.AuthData;
import model.GameData;
import model.UserData;
import requests.CreateGame;
import requests.JoinGame;
import requests.Response;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class GameService {
    InterfaceDOA<AuthData> authDOA;
    InterfaceDOA<UserData> userDOA;
    InterfaceDOA<GameData> gameDOA;

    public GameService(InterfaceDOA<AuthData> authDOA, InterfaceDOA<UserData> userDOA, InterfaceDOA<GameData> gameDOA) {
        this.authDOA = authDOA;
        this.userDOA = userDOA;
        this.gameDOA = gameDOA;
    }

    public Response joinGame(JoinGame request) {
        var serializer = new Gson();

        AuthData session = authDOA.get(request.authToken());

        if (session == null) {
            return new Response(401, serializer.toJson(Map.of("message", "Error: unauthorized")));
        }

        if (request.gameID() < 0 || !(Objects.equals(request.playerColor(), "WHITE") || Objects.equals(request.playerColor(), "BLACK"))) {
            return new Response(400, serializer.toJson(Map.of("message", "Error: bad request")));
        }

        Collection<GameData> games = gameDOA.list();

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
            gameDOA.replace(new GameData(game.get().gameID(), game.get().whiteUsername(),
                    session.username(), game.get().gameName(), game.get().game()));
        } else {
            gameDOA.replace(new GameData(game.get().gameID(), session.username(),
                    game.get().blackUsername(), game.get().gameName(), game.get().game()));
        }

        return new Response(200, "{}");
    }

    public Response getGames(String authToken) {
        var serializer = new Gson();

        AuthData session = authDOA.get(authToken);

        if (session == null) {
            return new Response(401, serializer.toJson(Map.of("message", "Error: unauthorized")));
        }

        var games = gameDOA.list();

        System.out.print("Games: " + games);
        return new Response(200, serializer.toJson(Map.of("games", games)));
    }

    public Response createGame(CreateGame request) {
        var serializer = new Gson();

        AuthData session = authDOA.get(request.authToken());

        if (session == null) {
            return new Response(401, serializer.toJson(Map.of("message", "Error: unauthorized")));
        }

        GameData newGame = request.game();

        if (newGame.gameName() == null) {
            return new Response(400, serializer.toJson(Map.of("message", "Error: bad request")));
        }

        GameData oldGame = gameDOA.get(null);

        int gameID = 1;
        if (oldGame != null) {
            gameID = oldGame.gameID() + 1;
        }

        gameDOA.create(new GameData(gameID, null, null, newGame.gameName(), null));

        return new Response(200, serializer.toJson(Map.of("gameID", gameID)));
    }
}
