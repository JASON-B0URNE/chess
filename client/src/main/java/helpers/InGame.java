package helpers;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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

    public InGame(String status, String gameStatus, ChessBoard chessBoard,
                  ChessGame chessGame, ChessGame.TeamColor color) {
        this.status = status;
        this.gameStatus = gameStatus;
        this.chessBoard = chessBoard;
        this.chessGame = chessGame;
        this.color = color;
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
                !Objects.equals(status, "LOGGED_IN") || !Objects.equals(gameStatus, "OBSERVING")) {
            invalidCommand();
            return;
        }

        gameStatus = "OUT_OF_GAME";
        System.out.print(SET_TEXT_COLOR_GREEN + "You have left successfully." + RESET_TEXT_COLOR + "\n");
    }

    public void resign(ArrayList<String> commandList) {
        if (commandList.size() != 1 ||
                !Objects.equals(status, "LOGGED_IN") || !Objects.equals(gameStatus, "PLAYING")) {
            invalidCommand();
            return;
        }

        gameStatus = "OUT_OF_GAME";
        System.out.print(SET_TEXT_COLOR_GREEN + "You have resigned successfully." + RESET_TEXT_COLOR + "\n");
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
