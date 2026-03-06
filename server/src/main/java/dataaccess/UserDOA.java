package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

import static dataaccess.DatabaseManager.*;

public class UserDOA implements InterfaceDOA<UserData> {
    private static final Logger log = LoggerFactory.getLogger(UserDOA.class);

    @Override
    public void create(UserData user) {
        try {
            String password = BCrypt.hashpw(user.password(), BCrypt.gensalt());

            executeUpdate("INSERT INTO users VALUES('" +
                    user.username() + "','" + password + "','" + user.email() + "');");
        } catch (SQLException _) {
            System.out.print("User Create Issues");
        }
    }

    @Override
    public UserData get(String username) {
        try {
            ArrayList<ArrayList<String>> result = executeQuery("SELECT username, password, email FROM users WHERE username='" + username + "';");
            if(result.isEmpty()) {
                System.out.print("NULL RESULT");
                return null;
            }
            return new UserData(result.get(0).get(0), result.get(0).get(1), result.get(0).get(2));
        } catch (SQLException ex) {
            System.out.print("User get error");
            return null;
        }
    }

    @Override
    public void delete(UserData data) {
    }

    @Override
    public void clear() {
        try {
            executeUpdate("DELETE FROM users;");
        } catch (SQLException _) {
            System.out.print("Clear Issue");
        }
    }

    @Override
    public Collection<UserData> list() {
        Collection<UserData> users = new HashSet<>();

        try {
            ArrayList<ArrayList<String>> result = executeQuery("SELECT username, password, email FROM users;");
            if(result.isEmpty()) {
                return users;
            }

            for (ArrayList<String> row : result) {
                users.add(new UserData(row.get(0), row.get(1), row.get(2)));
            }

            return users;
        } catch (SQLException ex) {
            System.out.print("List Users");
            return users;
        }
    }

    @Override
    public void replace(UserData data) {
    }

}
