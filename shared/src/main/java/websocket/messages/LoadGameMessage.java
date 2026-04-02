package websocket.messages;

import chess.ChessGame;

public class LoadGameMessage {
    ServerMessage.ServerMessageType serverMessageType = ServerMessage.ServerMessageType.LOAD_GAME;
    ChessGame game;

    public LoadGameMessage(ChessGame game) {
        this.game = game;
    }

    public ServerMessage.ServerMessageType getServerMessageType() {return this.serverMessageType;}
    public ChessGame getGame() {return this.game;}
}
