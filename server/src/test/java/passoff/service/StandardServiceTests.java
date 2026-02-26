package passoff.service;

import dataaccess.AuthDOA;
import dataaccess.GameDOA;
import dataaccess.InterfaceDOA;
import dataaccess.UserDOA;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
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
        UserData retrievedUser = this.userDOA.get("username");
        Assertions.assertEquals(newUser, retrievedUser, "Create normal user was not successful.");
    }

    @Test
    @Order(3)
    @DisplayName("Create User Negative Test")
    public void createUserNegativeTest() {
        UserData newUser = new UserData("username", null, "email");
        this.userService.createUser(newUser);
        UserData retrievedUser = this.userDOA.get("username");
        Assertions.assertNull(retrievedUser, "Create bad user was not successful.");
    }
}
