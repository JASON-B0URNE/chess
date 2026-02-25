package server;

import dataaccess.AuthDOA;
import dataaccess.GameDOA;
import dataaccess.InterfaceDOA;
import dataaccess.UserDOA;
import io.javalin.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import requests.Response;
import services.AuthService;
import services.GameService;
import services.UserService;

public class Server {
    private final Javalin javalin;

    public Server() {
        InterfaceDOA<AuthData> authDOA = new AuthDOA();
        InterfaceDOA<UserData> userDOA = new UserDOA();
        InterfaceDOA<GameData> gameDOA = new GameDOA();

        AuthService authService = new AuthService(authDOA, userDOA);
        UserService userService = new UserService(authDOA, userDOA);
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
                Response response = authService.createSession(ctx);
                ctx.status(response.code()).result(response.json());
            })
            .delete("/session", ctx -> {
                Response response = authService.deleteSession(ctx);
                ctx.status(response.code()).result(response.json());
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
