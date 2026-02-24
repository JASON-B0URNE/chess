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
            return games.getLast();
        }
    }

    @Override
    public void delete(GameData data) {

    }

    @Override
    public void clear() {
        this.games = new ArrayList<>();
    }

    @Override
    public Collection<GameData> list() {
        return this.games;
    }
}
