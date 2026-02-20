package server;

import io.javalin.*;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> {config.staticFiles.add("web");})
            .delete("/db", ctx -> {

            })
            .post("/user", ctx -> {

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
