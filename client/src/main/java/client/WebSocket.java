package client;

import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@ClientEndpoint
public class WebSocket {

    private static Session session;

    public String resign(UserGameCommand resign) throws IOException, ExecutionException, InterruptedException {
        var serializer = new Gson();
        CompletableFuture<String> future = new CompletableFuture<>();

        session.getBasicRemote().sendText(serializer.toJson(resign));

        return future.get();
    }

    public String leave(UserGameCommand leave) throws IOException, ExecutionException, InterruptedException {
        var serializer = new Gson();
        CompletableFuture<String> future = new CompletableFuture<>();

        session.getBasicRemote().sendText(serializer.toJson(leave));

        return future.get();
    }

    public String sendMove(MakeMoveCommand makeMove) throws IOException, ExecutionException, InterruptedException {
        var serializer = new Gson();
        CompletableFuture<String> future = new CompletableFuture<>();

        session.getBasicRemote().sendText(serializer.toJson(makeMove));

        return future.get();
    }

    public String connect(UserGameCommand connect) throws Exception {
        var serializer = new Gson();
        CompletableFuture<String> future = new CompletableFuture<>();

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        container.connectToServer(WebSocket.class, URI.create("ws://localhost:7000/ws"));

        session.getBasicRemote().sendText(serializer.toJson(connect));
        return future.get();
    }
}
