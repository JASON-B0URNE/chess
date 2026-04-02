package server;

import chess.ChessGame;
import com.google.gson.Gson;
import io.javalin.*;
import model.GameData;
import model.UserData;
import requests.CreateGame;
import requests.JoinGame;
import requests.Response;
import services.AuthService;
import services.GameService;
import services.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadGameMessage;
import websocket.messages.ServerMessage;

import java.sql.SQLException;
import java.util.Objects;

import static dataaccess.DatabaseManager.createDatabase;
import static dataaccess.DatabaseManager.createTables;

public class Server {
    private final Javalin javalin;

    public Server() {
        var serializer = new Gson();

        AuthService authService = new AuthService();
        UserService userService = new UserService();
        GameService gameService = new GameService();

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

                ChessGame game = gameService.getGame(message);

                // If game null then notify error

                if (Objects.equals(message.getCommandType(), UserGameCommand.CommandType.CONNECT)) {
                    LoadGameMessage loadMessage = new LoadGameMessage(game);

                    ctx.send(serializer.toJson(loadMessage));
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
