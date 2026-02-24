package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.AuthDOA;
import dataaccess.GameDOA;
import dataaccess.InterfaceDOA;
import dataaccess.UserDOA;
import io.javalin.*;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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

                int gameID = 0;
                if (oldGame != null) {
                    gameID = newGame.gameID() + 1;
                }
                System.out.print(gameID);
                gameDOA.create(new GameData(gameID, null, null, newGame.gameName(), null));
                ctx.status(200).result(serializer.toJson(Map.of("gameID", gameID)));
            })
            .put("/game", ctx -> {

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
