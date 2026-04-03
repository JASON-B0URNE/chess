package websocket.messages;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ErrorMessage {
    ServerMessage.ServerMessageType serverMessageType = ServerMessage.ServerMessageType.ERROR;
    String errorMessage;

    public ErrorMessage(String message) {
        this.errorMessage = message;
    }

    public ServerMessage.ServerMessageType getServerMessageType() {return this.serverMessageType;}
    public String getMessage() {return this.errorMessage;}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ErrorMessage that)) {
            return false;
        }
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
