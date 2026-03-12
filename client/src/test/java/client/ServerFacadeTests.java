package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dataaccess.AuthDOA;
import dataaccess.GameDOA;
import dataaccess.UserDOA;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import requests.Response;
import server.Server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static AuthDOA auth = new AuthDOA();
    private static UserDOA user = new UserDOA();
    private static GameDOA game = new GameDOA();


    @BeforeAll
    public static void init() throws Exception {
        server = new Server();
        var port = server.run(3333);
        facade = new ServerFacade(3333);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    @Order(1)
    public void clear() throws Exception {
        facade.clear();

        auth.create(new AuthData("authToken", "username"));
        user.create(new UserData("username", "password", "email"));
        game.create(new GameData(1, "gameName", null, null, null));

        facade.clear();

        Collection<AuthData> sessions = auth.list();
        Collection<UserData> users = user.list();
        Collection<GameData> games = game.list();

        Assertions.assertTrue(sessions.isEmpty() && users.isEmpty() && games.isEmpty());
    }

    @Test
    @Order(2)
    public void createUserPos() throws Exception {
        facade.createUser("username", "password", "email");

        Collection<UserData> users = user.list();

        Assertions.assertFalse(users.isEmpty());
    }

    @Test
    @Order(3)
    public void createUserNeg() throws Exception {
        facade.clear();

        facade.createUser("username", "password", "email");
        facade.createUser("username", "password", "email");

        Collection<UserData> users = user.list();

        Assertions.assertEquals(1, users.size());
    }

    @Test
    @Order(4)
    public void createSessionPos() throws Exception {
        var serializer = new Gson();
        Response response = facade.createUser("username6", "password", "email");

        AuthData session = serializer.fromJson(response.json(), AuthData.class);
        facade.deleteSession(session.authToken());

        response = facade.createSession("username6", "password");

        session = serializer.fromJson(response.json(), AuthData.class);
        facade.deleteSession(session.authToken());

        Assertions.assertEquals(200, response.code());
    }

    @Test
    @Order(5)
    public void createSessionNeg() throws Exception {
        Response response = facade.createSession("wrongusername", "password");

        Assertions.assertNotEquals(200, response.code());
    }

    @Test
    @Order(6)
    public void deleteSessionPos() throws Exception {
        var serializer = new Gson();
        Response response = facade.createUser("username1", "password", "email");

        AuthData session = serializer.fromJson(response.json(), AuthData.class);

        response = facade.deleteSession(session.authToken());

        Assertions.assertEquals(200, response.code());
    }

    @Test
    @Order(7)
    public void deleteSessionNeg() throws Exception {
        Response response = facade.deleteSession("authToken");

        Assertions.assertNotEquals(200, response.code());
    }

    @Test
    @Order(8)
    public void getGamesPos() throws Exception {
        var serializer = new Gson();
        Response response = facade.createUser("username2", "password", "email");

        AuthData session = serializer.fromJson(response.json(), AuthData.class);

        response = facade.getGames(session.authToken());

        Assertions.assertEquals(200, response.code());
    }

    @Test
    @Order(9)
    public void getGamesNeg() throws Exception {
        Response response = facade.getGames("authToken");

        Assertions.assertNotEquals(200, response.code());
    }

    @Test
    @Order(10)
    public void createGamePos() throws Exception {
        var serializer = new Gson();
        Response response = facade.createUser("username3", "password", "email");

        AuthData session = serializer.fromJson(response.json(), AuthData.class);

        response = facade.createGame(session.authToken(), "gameName");

        Assertions.assertEquals(200, response.code());
    }

    @Test
    @Order(11)
    public void createGameNeg() throws Exception {
        Response response = facade.createGame("authToken", "gameName");

        Assertions.assertNotEquals(200, response.code());
    }

    @Test
    @Order(12)
    public void joinGamePos() throws Exception {
        var serializer = new Gson();
        Response response = facade.createUser("username4", "password", "email");

        AuthData session = serializer.fromJson(response.json(), AuthData.class);

        response = facade.createGame(session.authToken(), "gameName");
        response = facade.createGame(session.authToken(), "gameName");

        GameData recentGame = game.get(null);

        response = facade.joinGame(session.authToken(), "WHITE", recentGame.gameID());

        Assertions.assertEquals(200, response.code());
    }

    @Test
    @Order(13)
    public void joinGameNeg() throws Exception {
        Response response = facade.joinGame("authToken", "WHITE", 1);

        Assertions.assertNotEquals(200, response.code());
    }
}
