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
import requests.CreateGame;
import requests.JoinGame;
import requests.Response;
import services.AuthService;
import services.GameService;
import services.UserService;

public class Server {
    private final Javalin javalin;

    public Server() {
        var serializer = new Gson();

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
