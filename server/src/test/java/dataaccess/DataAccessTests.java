package dataaccess;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import requests.CreateGame;
import requests.JoinGame;
import requests.Response;
import services.AuthService;
import services.GameService;
import services.UserService;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataAccessTests {
    InterfaceDOA<AuthData> authDOA;
    InterfaceDOA<UserData> userDOA;
    InterfaceDOA<GameData> gameDOA;

    AuthService authService;

    @BeforeEach
    public void setup() {
        this.authDOA = new AuthDOA();
        this.userDOA = new UserDOA();
        this.gameDOA = new GameDOA();

        this.authService = new AuthService();

        this.authService.clear();
    }

    @Test
    @Order(1)
    @DisplayName("Create User Positive Test")
    public void createUserPosTest() {
        try {
            this.userDOA.create(new UserData("username", "password", "email"));

            Collection<UserData> userList = this.userDOA.list();

            Assertions.assertFalse(userList.isEmpty(),
            "Create user positive was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(2)
    @DisplayName("Create User Negative Test")
    public void createUserNegTest() {
        try {
            this.userDOA.create(new UserData("username", "password", "email"));
            this.userDOA.create(new UserData("username", "password", "email"));

            Collection<UserData> userList = this.userDOA.list();

            Assertions.assertEquals(1, userList.size(),
                    "Create user negative was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(3)
    @DisplayName("Get User Positive Test")
    public void getUserPosTest() {
        try {
            this.userDOA.create(new UserData("username", "password", "email"));

            UserData user = this.userDOA.get("username");

            Assertions.assertEquals("username", user.username(),
                    "Get user positive was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(4)
    @DisplayName("Get User Negative Test")
    public void getUserNegTest() {
        try {
            UserData user = this.userDOA.get("username");

            Assertions.assertNull(user,
                    "Get user negative was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(5)
    @DisplayName("List User Positive Test")
    public void listUserPosTest() {
        try {
            this.userDOA.create(new UserData("username", "password", "email"));

            Collection<UserData> userList = this.userDOA.list();

            Assertions.assertFalse(userList.isEmpty(),
                    "List user positive was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(6)
    @DisplayName("List User Positive Test")
    public void listUserNegTest() {
        try {
            Collection<UserData> userList = this.userDOA.list();

            Assertions.assertTrue(userList.isEmpty(),
                    "List user negative was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(7)
    @DisplayName("Clear User Positive Test")
    public void clearUserPosTest() {
        try {
            this.userDOA.create(new UserData("username", "password", "email"));
            this.userDOA.clear();
            Collection<UserData> userList = this.userDOA.list();

            Assertions.assertTrue(userList.isEmpty(),
                    "Clear user positive was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(8)
    @DisplayName("Create Game Positive Test")
    public void createGamePosTest() {
        try {
            this.gameDOA.create(new GameData(1, "white", "black", "game name", null));

            Collection<GameData> gameList = this.gameDOA.list();

            Assertions.assertFalse(gameList.isEmpty(),
                    "Create game positive was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(9)
    @DisplayName("Create Game Negative Test")
    public void createGameNegTest() {
        try {
            this.gameDOA.create(new GameData(1, "white", "black", "game name", null));
            this.gameDOA.create(new GameData(1, "white", "black", "game name", null));

            Collection<GameData> gameList = this.gameDOA.list();

            Assertions.assertEquals(1, gameList.size(),
                    "Create game positive was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(10)
    @DisplayName("Get Game Positive Test")
    public void getGamePosTest() {
        try {
            this.gameDOA.create(new GameData(1, "white", "black", "game name", null));
            this.gameDOA.create(new GameData(2, "white", "black", "game name", null));

            GameData game = this.gameDOA.get(null);

            Assertions.assertEquals(2, game.gameID(),
                    "Get game positive was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(11)
    @DisplayName("Get Game Negative Test")
    public void getGameNegTest() {
        try {
            GameData game = this.gameDOA.get(null);

            Assertions.assertNull(game,
                    "Get game negative was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(12)
    @DisplayName("Delete Game Positive Test")
    public void deleteGamePosTest() {
        try {
            GameData game = new GameData(1, "white", "black", "game name", null);
            this.gameDOA.create(game);
            this.gameDOA.delete(game);

            Collection<GameData> gameList = this.gameDOA.list();

            Assertions.assertTrue(gameList.isEmpty(),
                    "Delete game positive was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(13)
    @DisplayName("Delete Game Negative Test")
    public void deleteGameNegTest() {
        try {
            GameData game = new GameData(1, "white", "black", "game name", null);
            GameData falseGame = new GameData(2, "white", "black", "game name", null);

            this.gameDOA.create(game);
            this.gameDOA.delete(falseGame);

            Collection<GameData> gameList = this.gameDOA.list();

            Assertions.assertFalse(gameList.isEmpty(),
                    "Delete game negative was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(14)
    @DisplayName("Clear Game Positive Test")
    public void clearGamePosTest() {
        try {
            GameData game = new GameData(1, "white", "black", "game name", null);
            this.gameDOA.create(game);
            this.gameDOA.clear();

            Collection<GameData> gameList = this.gameDOA.list();

            Assertions.assertTrue(gameList.isEmpty(),
                    "Clear game positive was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(15)
    @DisplayName("List Game Positive Test")
    public void listGamePosTest() {
        try {
            GameData game = new GameData(1, "white", "black", "game name", null);
            this.gameDOA.create(game);

            Collection<GameData> gameList = this.gameDOA.list();

            Assertions.assertFalse(gameList.isEmpty(),
                    "List game positive was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(16)
    @DisplayName("List Game Negative Test")
    public void listGameNegTest() {
        try {
            Collection<GameData> gameList = this.gameDOA.list();

            Assertions.assertTrue(gameList.isEmpty(),
                    "List game negative was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(17)
    @DisplayName("Replace Game Positive Test")
    public void replaceGamePosTest() {
        try {
            GameData game = new GameData(1, "white", "black", "game name", null);
            GameData replaceGame = new GameData(1, "testing", "black", "game name", null);

            this.gameDOA.create(game);
            this.gameDOA.replace(replaceGame);

            GameData newGame = this.gameDOA.get(null);

            Assertions.assertEquals("testing", newGame.whiteUsername(),
                    "Replace game positive was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(18)
    @DisplayName("Replace Game Negative Test")
    public void replaceGameNegTest() {
        try {
            GameData replaceGame = new GameData(1, "testing", "black", "game name", null);

            this.gameDOA.replace(replaceGame);

            GameData newGame = this.gameDOA.get(null);

            Assertions.assertNull(newGame,
                    "Replace game negative was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(19)
    @DisplayName("Create Auth Positive Test")
    public void createAuthPosTest() {
        try {
            AuthData session = new AuthData("token", "username");

            this.authDOA.create(session);

            Collection<AuthData> authList = this.authDOA.list();

            Assertions.assertFalse(authList.isEmpty(),
                    "Create session positive was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(20)
    @DisplayName("Create Auth Negative Test")
    public void createAuthNegTest() {
        try {
            AuthData session = new AuthData("token", "username");

            this.authDOA.create(session);
            this.authDOA.create(session);

            Collection<AuthData> authList = this.authDOA.list();

            Assertions.assertEquals(1, authList.size(),
                    "Create session negative was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(21)
    @DisplayName("Get Auth Positive Test")
    public void getAuthPosTest() {
        try {
            AuthData session = new AuthData("token", "username");

            this.authDOA.create(session);

            AuthData auth = this.authDOA.get("token");

            Assertions.assertEquals("token", auth.authToken(),
                    "Get session positive was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(22)
    @DisplayName("Get Auth Negative Test")
    public void getAuthNegTest() {
        try {
            AuthData auth = this.authDOA.get("token");

            Assertions.assertNull(auth,
                    "Create session positive was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(23)
    @DisplayName("Clear Auth Positive Test")
    public void clearAuthPosTest() {
        try {
            AuthData session = new AuthData("token", "username");
            this.authDOA.create(session);
            this.authDOA.clear();

            Collection<AuthData> authList = this.authDOA.list();

            Assertions.assertTrue(authList.isEmpty(),
                    "Clear session positive was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(24)
    @DisplayName("List Auth Positive Test")
    public void listAuthPosTest() {
        try {
            AuthData session = new AuthData("token", "username");
            this.authDOA.create(session);

            Collection<AuthData> authList = this.authDOA.list();

            Assertions.assertFalse(authList.isEmpty(),
                    "List session positive was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(25)
    @DisplayName("List Auth Negative Test")
    public void listAuthNegTest() {
        try {
            Collection<AuthData> authList = this.authDOA.list();

            Assertions.assertTrue(authList.isEmpty(),
                    "List session negative was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(26)
    @DisplayName("Delete Auth Positive Test")
    public void deleteAuthPosTest() {
        try {
            AuthData session = new AuthData("token", "username");
            this.authDOA.create(session);
            this.authDOA.delete(session);

            Collection<AuthData> authList = this.authDOA.list();

            Assertions.assertTrue(authList.isEmpty(),
                    "Delete session positive was not successful.");
        } catch (SQLException ex){}
    }

    @Test
    @Order(27)
    @DisplayName("List Auth Negative Test")
    public void deleteAuthNegTest() {
        try {
            AuthData session = new AuthData("token", "username");
            AuthData newSession = new AuthData("fake token", "username");

            this.authDOA.create(session);
            this.authDOA.delete(newSession);

            Collection<AuthData> authList = this.authDOA.list();

            Assertions.assertFalse(authList.isEmpty(),
                    "List session negative was not successful.");
        } catch (SQLException ex){}
    }
}
