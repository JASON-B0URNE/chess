package requests;

import model.GameData;

import java.util.Objects;

public record CreateGame(String authToken, GameData game) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreateGame that = (CreateGame) o;
        return Objects.equals(game(), that.game()) && Objects.equals(authToken(), that.authToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(authToken(), game());
    }
}
