package services;

import dataaccess.AuthDOA;
import dataaccess.GameDOA;
import dataaccess.InterfaceDOA;
import dataaccess.UserDOA;
import model.AuthData;
import model.GameData;
import model.UserData;
import requests.JoinGame;
import requests.Response;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class gameService {
//    static InterfaceDOA<AuthData> authDOA = new AuthDOA();
//    static InterfaceDOA<GameData> gameDOA = new GameDOA();
//
//    public static Response joinGame(JoinGame request) {
//        if (request.gameID() < 0 || !(Objects.equals(request.playerColor(), "WHITE") || Objects.equals(request.playerColor(), "BLACK"))) {
//            return new Response(400, Map.of("message", "Error: bad request"));
//        }
//
//        Collection<GameData> games = gameDOA.list();
//
//        AtomicReference<GameData> game = new AtomicReference<>();
//
//        games.forEach( x -> {
//            if (Objects.equals(x.gameID(), request.gameID())) {
//                game.set(x);
//            }
//        });
//
//        if (game.get() == null) {
//            return new Response(400, Map.of("message", "Error: unauthorized"));
//        }
//
//        if ((game.get().blackUsername() != null && Objects.equals(request.playerColor(), "BLACK")) ||
//        (game.get().whiteUsername() != null && Objects.equals(request.playerColor(), "WHITE"))) {
//            return new Response(403, Map.of("message", "Error: already taken"));
//        }
//
//        if (Objects.equals(request.playerColor(), "BLACK")) {
//            gameDOA.replace(new GameData(game.get().gameID(), game.get().whiteUsername(),
//            session.username(), game.get().gameName(), game.get().game()));
//        } else {
//            gameDOA.replace(new GameData(game.get().gameID(), session.username(),
//            game.get().blackUsername(), game.get().gameName(), game.get().game()));
//        }
//
//        return new Response(200, null);
//    }

}
