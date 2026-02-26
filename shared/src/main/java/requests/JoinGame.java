package requests;

import java.util.Objects;

public record JoinGame(String authToken, String playerColor, int gameID) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JoinGame joinGame = (JoinGame) o;
        return gameID() == joinGame.gameID() && Objects.equals(authToken(), joinGame.authToken())
        && Objects.equals(playerColor(), joinGame.playerColor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(authToken(), playerColor(), gameID());
    }
}