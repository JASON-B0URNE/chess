package services;

import com.google.gson.Gson;
import dataaccess.AuthDOA;
import dataaccess.InterfaceDOA;
import dataaccess.UserDOA;
import model.AuthData;
import model.UserData;
import requests.Response;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class UserService {
    InterfaceDOA<AuthData> authDOA = new AuthDOA();
    InterfaceDOA<UserData> userDOA = new UserDOA();

    public UserService() {
    }

    public Response createUser(UserData newUser) {
        var serializer = new Gson();
        UserData oldUser;

        try {
            oldUser = userDOA.get(newUser.username());
        } catch (SQLException ex) {
            return new Response(500, serializer.toJson(Map.of("message", "Error: database error")));
        }
        if (newUser.username() == null || newUser.password() == null || newUser.email() == null) {
            return new Response(400, serializer.toJson(Map.of("message", "Error: bad request")));
        }

        if (oldUser != null) {
            return new Response(403, serializer.toJson(Map.of("message", "Error: already taken")));
        }

        try {
            userDOA.create(newUser);
        } catch (SQLException ex) {
            return new Response(500, serializer.toJson(Map.of("message", "Error: database error")));
        }

        String authToken = UUID.randomUUID().toString();
        AuthData session = new AuthData( authToken, newUser.username());

        try {
            authDOA.create(session);
        } catch (SQLException ex) {
            return new Response(500, serializer.toJson(Map.of("message", "Error: database error")));
        }

        return new Response(200, serializer.toJson(session));
    }
}
