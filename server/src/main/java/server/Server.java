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
import requests.Response;
import services.AuthService;
import services.GameService;
import services.UserService;

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

        AuthService authService = new AuthService(authDOA, userDOA, gameDOA);
        UserService userService = new UserService(authDOA, userDOA, gameDOA);
        GameService gameService = new GameService(authDOA, userDOA, gameDOA);

        javalin = Javalin.create(config -> {config.staticFiles.add("web");})
            .delete("/db", ctx -> {
                authDOA.clear();
                userDOA.clear();
                gameDOA.clear();

                ctx.status(200).result("{}");
            })
            .post("/user", ctx -> {
                Response response = userService.createUser(ctx);
                ctx.status(response.code()).result(response.json());
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
                Response response = gameService.getGames(ctx);
                ctx.status(response.code()).result(response.json());
            })
            .post("/game", ctx -> {
                Response response = gameService.createGame(ctx);
                ctx.status(response.code()).result(response.json());
            })
            .put("/game", ctx -> {
                Response response = gameService.joinGame(ctx);
                ctx.status(response.code()).result(response.json());
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
