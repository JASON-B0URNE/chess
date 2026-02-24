package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;import dataaccess.AuthDOA;import dataaccess.UserDOA;import io.javalin.*;import model.AuthData;import model.UserData;

public class Server {
    private final Javalin javalin;

    public Server() {
        var serializer = new Gson();
        javalin = Javalin.create(config -> {config.staticFiles.add("web");})
            .delete("/db", ctx -> {

            })
            .post("/user", ctx -> {
                var json = serializer.toJson(ctx.body());
                UserData newUser = serializer.fromJson(json, UserData.class);

                if (UserDOA.get(newUser.username()) != null) {
                    ctx.status(403).result("Error: already taken");
                }

                if (newUser.password() == null || newUser.email() == null) {
                    ctx.status(400).result("Error: bad request");
                }


                UserDOA.create(newUser);
                String authToken =
                AuthData session = new AuthData(newUser.username(), );

                AuthDOA.create();

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
}
