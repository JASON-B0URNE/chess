package client;

import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static helpers.PrintBoard.printBoard;
import static ui.EscapeSequences.*;

@ClientEndpoint
public class WebSocket {
    private static Session session;

    @OnMessage
    public void onMessage(String message) throws IOException, ExecutionException, InterruptedException {
        var serializer = new Gson();
        try {
            if (message.contains("NOTIFICATION")) {
                NotificationMessage notification = serializer.fromJson(
                        message, NotificationMessage.class
                );
                String notificationMessage = notification.getMessage();
                System.out.print(SET_TEXT_COLOR_GREEN + notificationMessage + RESET_TEXT_COLOR + "\n");
                System.out.print("[PLAYING] >>> ");
            } else if (message.contains("ERROR")) {
                ErrorMessage error = serializer.fromJson(
                        message, ErrorMessage.class
                );

                String notificationMessage = error.getMessage();
                System.out.print(SET_TEXT_COLOR_RED + notificationMessage + RESET_TEXT_COLOR + "\n");
                System.out.print("[PLAYING] >>> ");
            } else {
                LoadGameMessage command = serializer.fromJson(
                        message, LoadGameMessage.class
                );
                System.out.print("\n");
                ClientMain.chessGame = command.getGame();
                ClientMain.chessBoard = ClientMain.chessGame.getBoard();
                ClientMain.parseCommand("redraw");
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void resign(UserGameCommand resign) throws IOException, ExecutionException, InterruptedException {
        var serializer = new Gson();

        session.getBasicRemote().sendText(serializer.toJson(resign));
    }

    public void leave(UserGameCommand leave) throws IOException, ExecutionException, InterruptedException {
        var serializer = new Gson();

        session.getBasicRemote().sendText(serializer.toJson(leave));
    }

    public void sendMove(MakeMoveCommand makeMove) throws IOException, ExecutionException, InterruptedException {
        var serializer = new Gson();

        session.getBasicRemote().sendText(serializer.toJson(makeMove));
    }

    public void connect(UserGameCommand connect) throws Exception {
        var serializer = new Gson();

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, URI.create("ws://localhost:8080/ws"));

        session.getBasicRemote().sendText(serializer.toJson(connect));
    }
}
