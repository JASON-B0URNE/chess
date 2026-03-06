package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import static dataaccess.DatabaseManager.executeQuery;
import static dataaccess.DatabaseManager.executeUpdate;

public class GameDOA implements InterfaceDOA<GameData> {
    @Override
    public void create(GameData game) {
        var serializer = new Gson();
        try {
            executeUpdate("INSERT INTO games VALUES(" +
                    game.gameID() + ", " + game.whiteUsername() + "," + game.blackUsername() +
                    "," + game.gameName() + "," + serializer.toJson(game.game()) + ");");
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
            return new GameData(gameID, row.get(1), row.get(2), row.get(3), game);
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
                games.add(new GameData(gameID, row.get(1), row.get(2), row.get(3), game));
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
            executeUpdate("UPDATE games " +
                    "SET whiteUsername='" + data.whiteUsername() +
                    "' blackUsername='" + data.blackUsername() +
                    "' gameName='" + data.gameName() +
                    "' game='" + serializer.toJson(data.game()) +
                    "WHERE gameID='" + data.gameID() + "';");
        } catch (SQLException _) {
            System.out.print("Game Replace Issue");
        }
    }
}
