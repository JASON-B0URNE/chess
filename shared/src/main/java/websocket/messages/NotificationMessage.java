package websocket.messages;

import chess.ChessGame;

public class NotificationMessage {
    ServerMessage.ServerMessageType serverMessageType = ServerMessage.ServerMessageType.NOTIFICATION;
    String message;

    public NotificationMessage(String message) {
        this.message = message;
    }

    public ServerMessage.ServerMessageType getServerMessageType() {return this.serverMessageType;}
    public String getMessage() {return this.message;}
}
