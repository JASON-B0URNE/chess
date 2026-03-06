package services;

import com.google.gson.Gson;
import dataaccess.AuthDOA;
import dataaccess.InterfaceDOA;
import dataaccess.UserDOA;
import model.AuthData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import requests.Response;

import java.util.Map;
import java.util.UUID;

public class AuthService {
    InterfaceDOA<AuthData> authDOA = new AuthDOA();
    InterfaceDOA<UserData> userDOA = new UserDOA();

    public AuthService() {
    }

    public Response createSession(UserData newUser) {
        var serializer = new Gson();

        UserData oldUser = userDOA.get(newUser.username());

        System.out.print("CHECK " + serializer.toJson(oldUser));

        if (newUser.username() == null || newUser.password() == null ) {
            return new Response(400, serializer.toJson(Map.of("message", "Error: bad request")));
        }

        if (oldUser == null || !newUser.password().equals(oldUser.password())) {
            return new Response(401, serializer.toJson(Map.of("message", "Error: unauthorized")));
        }

        String authToken = UUID.randomUUID().toString();
        AuthData session = new AuthData( authToken, newUser.username());

        authDOA.create(session);

        return new Response(200, serializer.toJson(session));
    }

    public Response deleteSession(String authToken) {
        var serializer = new Gson();

        AuthData session = authDOA.get(authToken);
        if (session == null) {
            return new Response(401, serializer.toJson(Map.of("message", "Error: unauthorized")));
        }

        authDOA.delete(session);
        return new Response(200, "{}");
    }

    public void clear() {
        authDOA.clear();
    }
}
