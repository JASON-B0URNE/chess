package services;

import dataaccess.InterfaceDOA;
import model.AuthData;
import model.GameData;
import model.UserData;

public class AuthService {
    InterfaceDOA<AuthData> authDOA;
    InterfaceDOA<UserData> userDOA;
    InterfaceDOA<GameData> gameDOA;

    public AuthService(InterfaceDOA<AuthData> authDOA, InterfaceDOA<UserData> userDOA, InterfaceDOA<GameData> gameDOA) {
        this.authDOA = authDOA;
        this.userDOA = userDOA;
        this.gameDOA = gameDOA;
    }
}
