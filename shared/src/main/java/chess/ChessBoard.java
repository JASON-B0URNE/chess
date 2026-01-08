package chess;

import java.util.ArrayList;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private static int CHESS_BOARD_LENGTH = 8;
    private ChessPiece[][] chessBoard;


    private void setupBackRow(String color) {

    }
    private void setupChessBoard() {
        this.chessBoard = new ChessPiece[8][8];
        this.chessBoard.add(setUpBackRow)

    }

    public ChessBoard() {
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        int modifiedCol = position.getColumn() - 1;
        int modifiedRow = position.getRow() - 1;

        this.chessBoard[modifiedRow][modifiedCol] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        int modifiedCol = position.getColumn() - 1;
        int modifiedRow = position.getRow() - 1;

        return this.chessBoard[modifiedRow][modifiedCol];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        setupChessBoard();
    }
}
