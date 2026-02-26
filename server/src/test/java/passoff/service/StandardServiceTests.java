package passoff.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dataaccess.AuthDOA;
import dataaccess.GameDOA;
import dataaccess.InterfaceDOA;
import dataaccess.UserDOA;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import requests.CreateGame;
import requests.JoinGame;
import requests.Response;
import services.AuthService;
import services.GameService;
import services.UserService;

import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StandardServiceTests {
    InterfaceDOA<AuthData> authDOA;
    InterfaceDOA<UserData> userDOA;
    InterfaceDOA<GameData> gameDOA;

    AuthService authService;
    UserService userService;
    GameService gameService;

    @BeforeEach
    public void setup() {
        this.authDOA = new AuthDOA();
        this.userDOA = new UserDOA();
        this.gameDOA = new GameDOA();

        this.authService = new AuthService(authDOA, userDOA);
        this.userService = new UserService(authDOA, userDOA);
        this.gameService = new GameService(authDOA, userDOA, gameDOA);
    }

    @Test
    @Order(1)
    @DisplayName("Clear Positive Test")
    public void clearTest() {
        this.authDOA.create(new AuthData("authToken", "username"));
        this.userDOA.create(new UserData("username", "password", "email"));
        this.gameDOA.create(new GameData(1234, "white", "black",
"gameName", null));

        this.authService.clear();
        this.userService.clear();
        this.gameService.clear();

        Collection<AuthData> authList = authDOA.list();
        Collection<AuthData> userList = authDOA.list();
        Collection<GameData> gameList = gameDOA.list();

        Assertions.assertTrue(authList.isEmpty() && userList.isEmpty() && gameList.isEmpty(),
        "Clear was not successful.");
    }

    @Test
    @Order(2)
    @DisplayName("Create User Positive Test")
    public void createUserPositiveTest() {
        UserData newUser = new UserData("username", "password", "email");
        this.userService.createUser(newUser);
        UserData retrievedUser = this.userDOA.get(newUser.username());
        Assertions.assertEquals(newUser, retrievedUser, "Create normal user was not successful.");
    }

    @Test
    @Order(3)
    @DisplayName("Create User Negative Test")
    public void createUserNegativeTest() {
        UserData newUser = new UserData("username", null, "email");
        this.userService.createUser(newUser);
        UserData retrievedUser = this.userDOA.get(newUser.username());
        Assertions.assertNull(retrievedUser, "Create bad user was not successful.");
    }

    @Test
    @Order(4)
    @DisplayName("Create Session Positive Test")
    public void createSessionPositiveTest() {
        UserData newUser = new UserData("username", "password", "email");
        this.authService.createSession(newUser);
        Collection<AuthData> sessions = this.authDOA.list();
        Assertions.assertTrue(sessions.isEmpty(), "Create normal user was not successful.");
    }

    @Test
    @Order(5)
    @DisplayName("Create Session Negative Test")
    public void createSessionNegativeTest() {
        UserData newUser = new UserData(null, "password", "email");
        this.authService.createSession(newUser);
        Collection<AuthData> sessions = this.authDOA.list();
        Assertions.assertTrue(sessions.isEmpty(), "Create bad user was not successful.");
    }

    @Test
    @Order(6)
    @DisplayName("Delete Session Positive Test")
    public void deleteSessionPositiveTest() {
        AuthData newSession = new AuthData("authToken", "username");
        this.authDOA.create(newSession);
        this.authService.deleteSession(newSession.authToken());
        Collection<AuthData> sessions = this.authDOA.list();
        Assertions.assertTrue(sessions.isEmpty(), "Delete normal session was not successful.");
    }

    @Test
    @Order(7)
    @DisplayName("Create Session Negative Test")
    public void deleteSessionNegativeTest() {
        AuthData newSession = new AuthData("authToken", "username");
        this.authDOA.create(newSession);
        this.authService.deleteSession(null);
        Collection<AuthData> sessions = this.authDOA.list();
        Assertions.assertFalse(sessions.isEmpty(), "Delete normal session was not successful.");
    }

    @Test
    @Order(8)
    @DisplayName("Get Games Positive Test")
    public void getGamesPositiveTest() {
        var serializer = new Gson();

        AuthData session = new AuthData("authToken", "username");

        this.authDOA.create(session);
        this.gameDOA.create(new GameData(1234, "white", "black",
        "name", null));

        Response result = this.gameService.getGames(session.authToken());

        Map<String, Collection<GameData>> json = serializer.fromJson(result.json(),
        new TypeToken<Map<String, Collection<GameData>>>() {}.getType());

        Collection<GameData> resultGames = json.get("games");

        Collection<GameData> games = this.gameDOA.list();

        Assertions.assertEquals(resultGames, games, "Get games normal was not successful.");
    }

    @Test
    @Order(9)
    @DisplayName("Get Games Negative Test")
    public void getGamesNegativeTest() {
        var serializer = new Gson();

        AuthData session = new AuthData(null, "username");

        this.gameDOA.create(new GameData(1234, "white", "black",
        "name", null));

        Response result = this.gameService.getGames(session.authToken());

        Assertions.assertTrue(result.code() != 200, "Get games bad was not successful.");
    }

    @Test
    @Order(10)
    @DisplayName("Create Game Positive Test")
    public void createGamePositiveTest() {
        AuthData session = new AuthData("authToken", "username");

        this.authDOA.create(session);

        GameData newGame = new GameData(1234, "white", "black",
        "name", null);

        this.gameService.createGame(new CreateGame(session.authToken(), newGame));

        Collection<GameData> games = this.gameDOA.list();

        Assertions.assertFalse(games.isEmpty(), "Get games normal was not successful.");
    }

    @Test
    @Order(11)
    @DisplayName("Create Game Negative Test")
    public void createGameNegativeTest() {
        AuthData session = new AuthData("authToken", "username");

        this.authDOA.create(session);

        GameData newGame = new GameData(0, "white", "black",
        null, null);

        this.gameService.createGame(new CreateGame(session.authToken(), newGame));

        Collection<GameData> games = this.gameDOA.list();

        Assertions.assertTrue(games.isEmpty(), "Get games normal was not successful.");
    }

    @Test
    @Order(12)
    @DisplayName("Join Game Positive Test")
    public void joinGamePositiveTest() {
        AuthData session = new AuthData("authToken", "username");

        this.authDOA.create(session);

        GameData newGame = new GameData(1, null, null,
        "name", null);
        this.gameDOA.create(newGame);

        JoinGame request = new JoinGame(session.authToken(), "WHITE", 1);

        this.gameService.joinGame(request);

        GameData joinedGame = this.gameDOA.get(null);

        Assertions.assertEquals(joinedGame.whiteUsername(), session.username(), "Get games normal was not successful.");
    }

    @Test
    @Order(13)
    @DisplayName("Join Game Negative Test")
    public void joinGameNegativeTest() {
        AuthData session = new AuthData("authToken", "username");

        this.authDOA.create(session);

        GameData newGame = new GameData(1234, "white", "black",
                "name", null);
        this.gameDOA.create(newGame);

        JoinGame request = new JoinGame(session.authToken(), "WHITE", 1234);

        this.gameService.joinGame(request);

        GameData joinedGame = this.gameDOA.get(null);

        Assertions.assertNotEquals(joinedGame.whiteUsername(), session.username(), "Get games normal was not successful.");
    }
}
