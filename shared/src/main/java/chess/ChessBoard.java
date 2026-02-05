package chess;
import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard implements Cloneable {
    private static final int CHESS_BOARD_LENGTH = 8;

    private static final int FRONT_EXECUTIVE_ROW_INDEX = 1;
    private static final int FRONT_PAWN_ROW_INDEX = 2;

    private static final int BACK_EXECUTIVE_ROW_INDEX = 8;
    private static final int BACK_PAWN_ROW_INDEX = 7;

    private static final int LEFT_ROOK_COL_INDEX = 1;
    private static final int RIGHT_ROOK_COL_INDEX = 8;

    private static final int LEFT_KNIGHT_COL_INDEX = 2;
    private static final int RIGHT_KNIGHT_COL_INDEX = 7;

    private static final int LEFT_BISHOP_COL_INDEX = 3;
    private static final int RIGHT_BISHOP_COL_INDEX = 6;

    private static final int QUEEN_COL_INDEX = 4;
    private static final int KING_COL_INDEX = 5;

    private ChessPiece[][] board;

    private void setupExecutiveRow(int rowIndex, ChessGame.TeamColor teamColor) {
        addPiece(new ChessPosition(rowIndex, LEFT_ROOK_COL_INDEX), new ChessPiece(teamColor, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(rowIndex, LEFT_KNIGHT_COL_INDEX), new ChessPiece(teamColor, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(rowIndex, LEFT_BISHOP_COL_INDEX), new ChessPiece(teamColor, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(rowIndex, QUEEN_COL_INDEX), new ChessPiece(teamColor, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(rowIndex, KING_COL_INDEX), new ChessPiece(teamColor, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(rowIndex, RIGHT_BISHOP_COL_INDEX), new ChessPiece(teamColor, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(rowIndex, RIGHT_KNIGHT_COL_INDEX), new ChessPiece(teamColor, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(rowIndex, RIGHT_ROOK_COL_INDEX), new ChessPiece(teamColor, ChessPiece.PieceType.ROOK));
    }

    private void setupPawnRow(int rowIndex, ChessGame.TeamColor teamColor) {
        for (int i = 1; i <= CHESS_BOARD_LENGTH; i++) {
            addPiece(new ChessPosition(rowIndex, i), new ChessPiece(teamColor, ChessPiece.PieceType.PAWN));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    private void setupChessBoard() {
        this.board = new ChessPiece[CHESS_BOARD_LENGTH][CHESS_BOARD_LENGTH];

        setupExecutiveRow(BACK_EXECUTIVE_ROW_INDEX, ChessGame.TeamColor.BLACK);
        setupPawnRow(BACK_PAWN_ROW_INDEX, ChessGame.TeamColor.BLACK);
        setupPawnRow(FRONT_PAWN_ROW_INDEX, ChessGame.TeamColor.WHITE);
        setupExecutiveRow(FRONT_EXECUTIVE_ROW_INDEX, ChessGame.TeamColor.WHITE);
    }

    public ChessBoard() {
        this.board = new ChessPiece[CHESS_BOARD_LENGTH][CHESS_BOARD_LENGTH];
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

        this.board[modifiedRow][modifiedCol] = piece;
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

        return this.board[modifiedRow][modifiedCol];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        setupChessBoard();
    }

    @Override
    public ChessBoard clone() {
        try {
            ChessBoard clone = (ChessBoard) super.clone();
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col ++) {
                    ChessPiece selectedPiece = this.board[row][col];

                    if (selectedPiece == null) {
                        clone.board[row][col] = null;
                    } else {
                        clone.board[row][col] = new ChessPiece(selectedPiece.getTeamColor(), selectedPiece.getPieceType());
                    }

                }
            }

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
