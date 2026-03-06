package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static dataaccess.DatabaseManager.*;

public class UserDOA implements InterfaceDOA<UserData> {
    @Override
    public void create(UserData user) throws SQLException {
        String password = BCrypt.hashpw(user.password(), BCrypt.gensalt());

        executeUpdate("INSERT INTO users VALUES('" +
                user.username() + "','" + password + "','" + user.email() + "');");
    }

    @Override
    public UserData get(String username) throws SQLException {
        ArrayList<ArrayList<String>> result = executeQuery("SELECT username, password, email FROM users WHERE username='" + username + "';");
        if(result.isEmpty()) {
            System.out.print("NULL RESULT");
            return null;
        }
        return new UserData(result.get(0).get(0), result.get(0).get(1), result.get(0).get(2));
    }

    @Override
    public void delete(UserData data) {
    }

    @Override
    public void clear() throws SQLException {
        executeUpdate("DELETE FROM users;");
    }

    @Override
    public Collection<UserData> list() throws SQLException {
        Collection<UserData> users = new HashSet<>();

        ArrayList<ArrayList<String>> result = executeQuery("SELECT username, password, email FROM users;");
        if(result.isEmpty()) {
            return users;
        }

        for (ArrayList<String> row : result) {
            users.add(new UserData(row.get(0), row.get(1), row.get(2)));
        }

        return users;
    }

    @Override
    public void replace(UserData data) {
    }

}
