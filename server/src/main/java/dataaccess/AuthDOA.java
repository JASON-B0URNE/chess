package dataaccess;

import model.AuthData;
import model.UserData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static dataaccess.DatabaseManager.executeQuery;
import static dataaccess.DatabaseManager.executeUpdate;

public class AuthDOA implements InterfaceDOA<AuthData> {
    @Override
    public void create(AuthData auth) {
        try {
            executeUpdate("INSERT INTO sessions VALUES('" +
                    auth.authToken() + "','" + auth.username() + "');");
        } catch (SQLException _) {
            System.out.print("Auth Create Issues");
        }
    }

    @Override
    public AuthData get(String authToken) {
        try {
            ArrayList<ArrayList<String>> result = executeQuery("SELECT authToken, username FROM sessions WHERE authToken='" + authToken + "';");
            if(result.isEmpty()) {
                return null;
            }
            return new AuthData(result.get(0).get(0), result.get(0).get(1));
        } catch (SQLException ex) {
            System.out.print("Session get error");
            return null;
        }
    }

    @Override
    public void clear() {
        try {
            executeUpdate("DELETE FROM sessions;");
        } catch (SQLException _) {
            System.out.print("Clear Sessions Issue");
        }
    }

    @Override
    public Collection<AuthData> list() {
        Collection<AuthData> authTokens = new HashSet<>();

        try {
            ArrayList<ArrayList<String>> result = executeQuery("SELECT authToken, username FROM sessions;");
            if(result.isEmpty()) {
                return authTokens;
            }

            for (ArrayList<String> row : result) {
                authTokens.add(new AuthData(row.get(0), row.get(1)));
            }

            return authTokens;
        } catch (SQLException ex) {
            System.out.print("List Users");
            return authTokens;
        }
    }

    @Override
    public void replace(AuthData data) {

    }

    @Override
    public void delete(AuthData auth) {
        try {
            executeUpdate("DELETE FROM sessions WHERE authToken='" + auth.authToken() + "';");
        } catch (SQLException _) {
            System.out.print("Auth Delete Issue");
        }
    }
}
