package dataaccess;

import model.AuthData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static dataaccess.DatabaseManager.executeQuery;
import static dataaccess.DatabaseManager.executeUpdate;

public class AuthDOA implements InterfaceDOA<AuthData> {
    @Override
    public void create(AuthData auth) throws SQLException {
        executeUpdate("INSERT INTO sessions VALUES('" +
                auth.authToken() + "','" + auth.username() + "');");

    }

    @Override
    public AuthData get(String authToken) throws SQLException {
        ArrayList<ArrayList<String>> result = executeQuery("SELECT authToken, username FROM sessions WHERE authToken='" + authToken + "';");
        if(result.isEmpty()) {
            return null;
        }
        return new AuthData(result.get(0).get(0), result.get(0).get(1));

    }

    @Override
    public void clear() throws SQLException {
        executeUpdate("DELETE FROM sessions;");
    }

    @Override
    public Collection<AuthData> list() throws SQLException {
        Collection<AuthData> authTokens = new HashSet<>();

        ArrayList<ArrayList<String>> result = executeQuery("SELECT authToken, username FROM sessions;");
        if(result.isEmpty()) {
            return authTokens;
        }

        for (ArrayList<String> row : result) {
            authTokens.add(new AuthData(row.get(0), row.get(1)));
        }

        return authTokens;
    }

    @Override
    public void replace(AuthData data) {

    }

    @Override
    public void delete(AuthData auth) throws SQLException {
        executeUpdate("DELETE FROM sessions WHERE authToken='" + auth.authToken() + "';");
    }
}
