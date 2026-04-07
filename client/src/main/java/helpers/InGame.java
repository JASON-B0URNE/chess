package helpers;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import client.ClientMain;
import client.WebSocket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Scanner;

import static helpers.Validation.validatePosition;
import static helpers.Output.invalidCommand;
import static helpers.PrintBoard.printBoard;
import static ui.EscapeSequences.*;

public class InGame {
    String status;
    String gameStatus;
    ChessBoard chessBoard;
    ChessGame chessGame;
    ChessGame.TeamColor color;
    WebSocket wsConnection;

    public InGame(String status, String gameStatus, ChessBoard chessBoard,
                  ChessGame chessGame, ChessGame.TeamColor color, WebSocket wsConnection) {
        this.status = status;
        this.gameStatus = gameStatus;
        this.chessBoard = chessBoard;
        this.chessGame = chessGame;
        this.color = color;
        this.wsConnection = wsConnection;
    }

    public void redraw(ArrayList<String> commandList) {
        if (commandList.size() != 1 ||
                !Objects.equals(status, "LOGGED_IN") || Objects.equals(gameStatus, "OUT_OF_GAME")) {
            invalidCommand();
            return;
        }

        printBoard(chessBoard, color, null);
    }

    public void leave(ArrayList<String> commandList) {
        if (commandList.size() != 1 ||
                !Objects.equals(status, "LOGGED_IN") || !(Objects.equals(gameStatus, "OBSERVING") ||
                    Objects.equals(gameStatus, "PLAYING"))) {
            invalidCommand();
            return;
        }

        ClientMain.gameStatus = "OUT_OF_GAME";
    }

    public Boolean resign(ArrayList<String> commandList) {
        if (commandList.size() != 1 ||
                !Objects.equals(status, "LOGGED_IN") || !Objects.equals(gameStatus, "PLAYING")) {
            invalidCommand();
        }
        System.out.println();
        System.out.print(SET_TEXT_COLOR_RED + "Are you sure you want to resign? [y|n]: " + RESET_TEXT_COLOR);
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();

        return line.equalsIgnoreCase("y");

    }

    public void highlight(ArrayList<String> commandList) {
        if (commandList.size() != 2 ||
                !Objects.equals(status, "LOGGED_IN") || !Objects.equals(gameStatus, "PLAYING")) {
            invalidCommand();
            return;
        }
        ChessPosition startPosition = validatePosition(commandList.get(1));

        if (!Objects.equals(chessBoard.getPiece(startPosition).getTeamColor(), color)) {
            System.out.print(SET_TEXT_COLOR_RED + "ERROR: Cannot highlight pieces of the opposite color." + RESET_TEXT_COLOR + "\n");
            return;
        }

        Collection<ChessMove> validMoves = chessGame.validMoves(startPosition);

        Collection<ChessPosition> validPositions = new ArrayList<ChessPosition>();
        validMoves.forEach(x -> validPositions.add(x.getEndPosition()));

        printBoard(chessBoard, color, validPositions);
    }
}
