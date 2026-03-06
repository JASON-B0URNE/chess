package services;

import com.google.gson.Gson;
import dataaccess.AuthDOA;
import dataaccess.InterfaceDOA;
import dataaccess.UserDOA;
import model.AuthData;
import model.UserData;
import requests.Response;

import java.util.Map;
import java.util.UUID;

public class UserService {
    InterfaceDOA<AuthData> authDOA = new AuthDOA();
    InterfaceDOA<UserData> userDOA = new UserDOA();

    public UserService() {
    }

    public Response createUser(UserData newUser) {
        var serializer = new Gson();

        UserData oldUser = userDOA.get(newUser.username());

        if (newUser.username() == null || newUser.password() == null || newUser.email() == null) {
            return new Response(400, serializer.toJson(Map.of("message", "Error: bad request")));
        }

        if (oldUser != null) {
            return new Response(403, serializer.toJson(Map.of("message", "Error: already taken")));
        }

        userDOA.create(newUser);
        String authToken = UUID.randomUUID().toString();
        AuthData session = new AuthData( authToken, newUser.username());

        authDOA.create(session);

        return new Response(200, serializer.toJson(session));
    }

    public void clear() {
        userDOA.clear();
    }
}
