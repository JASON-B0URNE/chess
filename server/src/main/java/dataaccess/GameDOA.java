package dataaccess;

import model.GameData;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class GameDOA implements InterfaceDOA<GameData> {
    private List<GameData> games = new ArrayList<>();

    @Override
    public void create(GameData game) {
        this.games.add(game);
    }

    @Override
    public GameData get(String str) {
        if (games.isEmpty()) {
            return null;
        } else {
            return games.get(games.size() - 1);
        }
    }

    @Override
    public void delete(GameData game) {
        this.games.remove(game);
    }

    @Override
    public void clear() {
        this.games = new ArrayList<>();
    }

    @Override
    public Collection<GameData> list() {
        return this.games;
    }

    @Override
    public void replace(GameData data) {
        this.games.set(data.gameID() - 1, data);
    }
}
