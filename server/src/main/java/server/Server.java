package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.AuthDOA;
import dataaccess.InterfaceDOA;
import dataaccess.UserDOA;
import io.javalin.*;
import model.AuthData;
import model.UserData;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Server {
    private final Javalin javalin;

    public Server() {
        var serializer = new Gson();
        InterfaceDOA<AuthData> authDOA = new AuthDOA();
        InterfaceDOA<UserData> userDOA = new UserDOA();

        javalin = Javalin.create(config -> {config.staticFiles.add("web");})
            .delete("/db", ctx -> {
                authDOA.clear();
                userDOA.clear();

                ctx.status(200).result(serializer.toJson(null));
            })
            .post("/user", ctx -> {
                UserData newUser = serializer.fromJson(ctx.body(), UserData.class);
                UserData oldUser = (UserData) userDOA.get(newUser.username());

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
                UserData oldUser = (UserData) userDOA.get(newUser.username());

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

                AuthData session = (AuthData) authDOA.get(authToken);
                System.out.print(ctx.body());
                if (session == null) {
                    ctx.status(401).result(serializer.toJson(Map.of("message", "Error: unauthorized")));
                    return;
                }

                authDOA.delete(session);
                ctx.status(200).result(serializer.toJson(null));
            })
            .get("/game", ctx -> {
                String authToken = ctx.header("Authorization");
                System.out.print(authToken);
                if (authToken == null) {
                    ctx.status(401).result("Error: unauthorized");
                }

            })
            .post("/game", ctx -> {

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
