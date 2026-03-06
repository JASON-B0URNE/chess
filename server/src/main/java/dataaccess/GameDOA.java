package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dataaccess.DatabaseManager.executeQuery;
import static dataaccess.DatabaseManager.executeUpdate;

public class GameDOA implements InterfaceDOA<GameData> {
    @Override
    public void create(GameData game) {
        var serializer = new Gson();
        try {
            System.out.println("GAME NAME: " + game.gameName());


            String gameName = game.gameName();
            if (gameName != null) {
                gameName = game.gameName().replace("'", "''");
            }

            executeUpdate("INSERT INTO games VALUES('" +
                    game.gameID() + "','" + game.whiteUsername() + "','" + game.blackUsername() +
                    "','" + gameName + "','" + serializer.toJson(game.game()) + "');");
        } catch (SQLException _) {
            System.out.print("User Create Issues");
        }
    }

    @Override
    public GameData get(String str) {
        try {
            ArrayList<ArrayList<String>> result = executeQuery("SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games ORDER BY gameID DESC LIMIT 1;");

            if(result.isEmpty()) {
                return null;
            }
            ArrayList<String> row = result.get(0);
            int gameID = Integer.parseInt(row.get(0));
            var serializer = new Gson();
            ChessGame game = serializer.fromJson(row.get(4), ChessGame.class);
            String whiteUsername = row.get(1);
            if (Objects.equals(whiteUsername, "null")) {
                whiteUsername = null;
            }
            String blackUsername = row.get(2);
            if (Objects.equals(blackUsername, "null")) {
                blackUsername = null;
            }
            String gameName = row.get(3);
            if (Objects.equals(gameName, "null")) {
                gameName = null;
            }
            return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
        } catch (SQLException ex) {
            System.out.print("Get Game");
            return null;
        }
    }

    @Override
    public void delete(GameData game) {
        try {
            executeUpdate("DELETE FROM games WHERE gameID='" + game.gameID() + "';");
        } catch (SQLException _) {
            System.out.print("Game Clear Issue");
        }
    }

    @Override
    public void clear() {
        try {
            executeUpdate("DELETE FROM games;");
        } catch (SQLException _) {
            System.out.print("Game Clear Issue");
        }
    }

    @Override
    public Collection<GameData> list() {
        List<GameData> games = new ArrayList<>();
        try {
            ArrayList<ArrayList<String>> result = executeQuery("SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games;");
            if(result.isEmpty()) {
                return games;
            }

            for (ArrayList<String> row : result) {
                int gameID = Integer.parseInt(row.get(0));
                var serializer = new Gson();
                ChessGame game = serializer.fromJson(row.get(4), ChessGame.class);
                String whiteUsername = row.get(1);
                if (Objects.equals(whiteUsername, "null")) {
                    whiteUsername = null;
                }
                String blackUsername = row.get(2);
                if (Objects.equals(blackUsername, "null")) {
                    blackUsername = null;
                }
                String gameName = row.get(3);
                if (Objects.equals(gameName, "null")) {
                    gameName = null;
                }
                games.add(new GameData(gameID, whiteUsername, blackUsername, gameName, game));
            }

            return games;
        } catch (SQLException ex) {
            System.out.print("Game List Error");
            return games;
        }
    }

    @Override
    public void replace(GameData data) {
        var serializer = new Gson();
        try {

            System.out.println("TESTING BLACK USERNAME: " + data.blackUsername());

            String gameName = data.gameName();
            if (gameName != null) {
                gameName = data.gameName().replace("'", "''");
            }

            executeUpdate("UPDATE games " +
                    "SET whiteUsername='" + data.whiteUsername() +
                    "', blackUsername='" + data.blackUsername() +
                    "', gameName='" + gameName +
                    "', game='" + serializer.toJson(data.game()) +
                    "' WHERE gameID='" + data.gameID() + "';");
        } catch (SQLException _) {
            System.out.print("Game Replace Issue");
        }
    }
}
