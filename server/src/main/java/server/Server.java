package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.AuthDOA;
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

        javalin = Javalin.create(config -> {config.staticFiles.add("web");})
            .delete("/db", ctx -> {

            })
            .post("/user", ctx -> {
                UserData newUser = serializer.fromJson(ctx.body(), UserData.class);
                UserData oldUser = UserDOA.get(newUser.username());

                if (oldUser != null) {
                    ctx.status(403).result(serializer.toJson(Map.of("message", "Error: already taken")));
                    return;
                }

                if (newUser.password() == null || newUser.email() == null) {
                    ctx.status(400).result(serializer.toJson(Map.of("message", "Error: bad request")));
                    return;
                }

                UserDOA.create(newUser);
                String authToken = UUID.randomUUID().toString();
                AuthData session = new AuthData( authToken, newUser.username());

                AuthDOA.create(session);

                ctx.status(200).result(serializer.toJson(session));
            })
            .post("/session", ctx -> {

            })
            .delete("/session", ctx -> {

            })
            .get("/game", ctx -> {
                String authToken = ctx.header("authorization");

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Server server = (Server) o;
        return Objects.equals(javalin, server.javalin);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(javalin);
    }
}
