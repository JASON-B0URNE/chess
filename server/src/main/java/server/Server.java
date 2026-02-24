package server;

import com.google.gson.Gson;
import dataaccess.AuthDOA;
import dataaccess.GameDOA;
import dataaccess.InterfaceDOA;
import dataaccess.UserDOA;
import io.javalin.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import requests.JoinGame;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Server {
    private final Javalin javalin;

    public Server() {
        var serializer = new Gson();
        InterfaceDOA<AuthData> authDOA = new AuthDOA();
        InterfaceDOA<UserData> userDOA = new UserDOA();
        InterfaceDOA<GameData> gameDOA = new GameDOA();

        javalin = Javalin.create(config -> {config.staticFiles.add("web");})
            .delete("/db", ctx -> {
                authDOA.clear();
                userDOA.clear();
                gameDOA.clear();

                ctx.status(200).result(serializer.toJson(null));
            })
            .post("/user", ctx -> {
                UserData newUser = serializer.fromJson(ctx.body(), UserData.class);
                UserData oldUser = userDOA.get(newUser.username());

                if (newUser.username() == null || newUser.password() == null || newUser.email() == null) {
                    ctx.status(400).result(serializer.toJson(Map.of("message", "Error: bad request")));
                    return;
                }

                if (oldUser != null) {
                    ctx.status(403).result(serializer.toJson(Map.of("message", "Error: already taken")));
                    return;
                }

                userDOA.create(newUser);
                String authToken = UUID.randomUUID().toString();
                AuthData session = new AuthData( authToken, newUser.username());

                authDOA.create(session);

                ctx.status(200).result(serializer.toJson(session));
            })
            .post("/session", ctx -> {
                UserData newUser = serializer.fromJson(ctx.body(), UserData.class);
                UserData oldUser = userDOA.get(newUser.username());

                if (newUser.username() == null || newUser.password() == null ) {
                    ctx.status(400).result(serializer.toJson(Map.of("message", "Error: bad request")));
                    return;
                }

                if (oldUser == null || !newUser.password().equals(oldUser.password())) {
                    ctx.status(401).result(serializer.toJson(Map.of("message", "Error: unauthorized")));
                    return;
                }

                String authToken = UUID.randomUUID().toString();
                AuthData session = new AuthData( authToken, newUser.username());

                authDOA.create(session);

                ctx.status(200).result(serializer.toJson(session));
            })
            .delete("/session", ctx -> {
                String authToken = ctx.header("Authorization");

                AuthData session = authDOA.get(authToken);
                if (session == null) {
                    ctx.status(401).result(serializer.toJson(Map.of("message", "Error: unauthorized")));
                    return;
                }

                authDOA.delete(session);
                ctx.status(200).result(serializer.toJson(null));
            })
            .get("/game", ctx -> {
                String authToken = ctx.header("Authorization");
                AuthData session = authDOA.get(authToken);

                if (session == null) {
                    ctx.status(401).result(serializer.toJson(Map.of("message", "Error: unauthorized")));
                    return;
                }

                var games = gameDOA.list();
                ctx.status(200).result(serializer.toJson(Map.of("games", games)));
            })
            .post("/game", ctx -> {
                String authToken = ctx.header("Authorization");
                AuthData session = authDOA.get(authToken);

                if (session == null) {
                    ctx.status(401).result(serializer.toJson(Map.of("message", "Error: unauthorized")));
                    return;
                }

                GameData newGame = serializer.fromJson(ctx.body(), GameData.class);

                if (newGame.gameName() == null) {
                    ctx.status(400).result(serializer.toJson(Map.of("message", "Error: bad request")));
                    return;
                }

                GameData oldGame = gameDOA.get(null);

                int gameID = 1;
                if (oldGame != null) {
                    gameID = oldGame.gameID() + 1;
                }

                gameDOA.create(new GameData(gameID, null, null, newGame.gameName(), null));
                ctx.status(200).result(serializer.toJson(Map.of("gameID", gameID)));
            })
            .put("/game", ctx -> {
                String authToken = ctx.header("Authorization");
                AuthData session = authDOA.get(authToken);

                if (session == null) {
                    ctx.status(401).result(serializer.toJson(Map.of("message", "Error: unauthorized")));
                    return;
                }

                JoinGame request = serializer.fromJson(ctx.body(), JoinGame.class);
                if (request.gameID() < 0 || !(Objects.equals(request.playerColor(), "WHITE") || Objects.equals(request.playerColor(), "BLACK"))) {
                    ctx.status(400).result(serializer.toJson(Map.of("message", "Error: bad request")));
                    return;
//                    return new Response(400, Map.of("message", "Error: bad request"));
                }

                Collection<GameData> games = gameDOA.list();

                AtomicReference<GameData> game = new AtomicReference<>();

                games.forEach( x -> {
                    if (Objects.equals(x.gameID(), request.gameID())) {
                        game.set(x);
                    }
                });

                if (game.get() == null) {
                    ctx.status(400).result(serializer.toJson(Map.of("message", "Error: bad request")));
                    return;
//                    return new Response(400, Map.of("message", "Error: unauthorized"));
                }

                if ((game.get().blackUsername() != null && Objects.equals(request.playerColor(), "BLACK")) ||
                        (game.get().whiteUsername() != null && Objects.equals(request.playerColor(), "WHITE"))) {
                    ctx.status(403).result(serializer.toJson(Map.of("message", "Error: already taken")));
                    return;
                }

                if (Objects.equals(request.playerColor(), "BLACK")) {
                    gameDOA.replace(new GameData(game.get().gameID(), game.get().whiteUsername(),
                            session.username(), game.get().gameName(), game.get().game()));
                } else {
                    gameDOA.replace(new GameData(game.get().gameID(), session.username(),
                            game.get().blackUsername(), game.get().gameName(), game.get().game()));
                }
                ctx.status(200).result(serializer.toJson(null));
//                return new Response(200, null);
            })
        ;
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
