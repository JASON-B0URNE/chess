package dataaccess;

import chess.ChessMove;
import model.AuthData;

import java.util.Collection;
import java.util.HashSet;

public class AuthDOA {
    private static Collection<AuthData> authTokens = new HashSet<>();

    public static void create(AuthData auth) {
        authTokens.add(auth);
    }


}
