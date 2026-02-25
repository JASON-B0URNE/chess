package services;

import com.google.gson.Gson;
import dataaccess.InterfaceDOA;
import model.AuthData;
import model.UserData;
import requests.Response;

import java.util.Map;
import java.util.UUID;

public class UserService {
    InterfaceDOA<AuthData> authDOA;
    InterfaceDOA<UserData> userDOA;

    public UserService(InterfaceDOA<AuthData> authDOA, InterfaceDOA<UserData> userDOA) {
        this.authDOA = authDOA;
        this.userDOA = userDOA;
    }

    public Response createUser(io.javalin.http.Context ctx) {
        var serializer = new Gson();

        UserData newUser = serializer.fromJson(ctx.body(), UserData.class);
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

}
