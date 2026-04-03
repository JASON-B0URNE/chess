package services;

import com.google.gson.Gson;
import dataaccess.AuthDOA;
import dataaccess.GameDOA;
import dataaccess.InterfaceDOA;
import dataaccess.UserDOA;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import org.mindrot.jbcrypt.BCrypt;
import requests.Response;
import websocket.commands.UserGameCommand;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class AuthService {
    InterfaceDOA<AuthData> authDOA = new AuthDOA();
    InterfaceDOA<UserData> userDOA = new UserDOA();
    InterfaceDOA<GameData> gameDOA = new GameDOA();

    public AuthService() {
    }

    public Response createSession(UserData newUser) {
        var serializer = new Gson();
        UserData oldUser;

        try {
            oldUser = userDOA.get(newUser.username());
        } catch (SQLException ex) {
            return new Response(500, serializer.toJson(Map.of("message", "Error: database error")));
        }

        if (newUser.username() == null || newUser.password() == null ) {
            return new Response(400, serializer.toJson(Map.of("message", "Error: bad request")));
        }



        if (oldUser == null || !BCrypt.checkpw(newUser.password(), oldUser.password())) {
            return new Response(401, serializer.toJson(Map.of("message", "Error: unauthorized")));
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

    public Response deleteSession(String authToken) {
        var serializer = new Gson();
        AuthData session;

        try {
            session = authDOA.get(authToken);
        } catch (SQLException ex) {
            return new Response(500, serializer.toJson(Map.of("message", "Error: database error")));
        }

        if (session == null) {
            return new Response(401, serializer.toJson(Map.of("message", "Error: unauthorized")));
        }

        try {
            authDOA.delete(session);
        } catch (SQLException ex) {
            return new Response(500, serializer.toJson(Map.of("message", "Error: database error")));
        }

        return new Response(200, "{}");
    }

    public Response clear() {
        var serializer = new Gson();

        try {
            gameDOA.clear();
            userDOA.clear();
            authDOA.clear();
            return new Response(200, serializer.toJson(null));
        } catch (SQLException ex) {
            return new Response(500, serializer.toJson(Map.of("message", "Error: database error")));
        }
    }

    public AuthData getUser(UserGameCommand command) {
        AuthData session;

        try {
            session = authDOA.get(command.getAuthToken());
        } catch (SQLException ex) {
            return null;
        }

        return session;
    }
}
