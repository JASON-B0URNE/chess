package chess;

import java.util.Collection;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private static final int CHESS_BOARD_LENGTH = 8;
    private static final int NO_INCREMENT = 0;
    private static final int INCREMENT_POSITIVE = 1;
    private static final int INCREMENT_NEGATIVE = -1;
    private static final int KNIGHT_PRIMARY_MOVEMENT = 2;
    private static final int KNIGHT_SECONDARY_MOVEMENT = 1;
    private static final int PAWN_START_MOVEMENT = 2;


    private final ChessGame.TeamColor teamColor;
    private final PieceType pieceType;
    private final Collection<ChessMove> possibleMoves = new HashSet<ChessMove>();;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.teamColor = pieceColor;
        this.pieceType = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */

    private void castlingCheck(ChessBoard board, Collection<ChessMove> moves, ChessPosition myPosition) {

    }

    private void incrementerCheck(int incrementRow, int incrementCol, ChessBoard board, ChessPosition myPosition) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        System.out.println("INITIAL CHECK - POS ROW: " + row + " POS COL: " + col);

        row += incrementRow;

        while (row <= CHESS_BOARD_LENGTH && row > 0) {
            col += incrementCol;

            System.out.println("INCREMENTER CHECK - POS ROW: " + row + " POS COL: " + col);
            if (! validationCheck(row, col, board, myPosition)) {
                break;
            }

            row += incrementRow;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChessPiece that = (ChessPiece) o;
        return teamColor == that.teamColor && pieceType == that.pieceType && possibleMoves.equals(that.possibleMoves);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(teamColor);
        result = 31 * result + Objects.hashCode(pieceType);
        result = 31 * result + possibleMoves.hashCode();
        return result;
    }

    private boolean validationCheck(int row, int col, ChessBoard board, ChessPosition currentPosition) {
        System.out.println("CHECKED MOVE - POS ROW: " + row + " POS COL: " + col);
        if (col <= CHESS_BOARD_LENGTH && col > 0 && row <= CHESS_BOARD_LENGTH && row > 0) {
            ChessPosition finalPosition = new ChessPosition(row, col);
            ChessPiece selectedPiece = board.getPiece(finalPosition);

            if(selectedPiece != null) {
                System.out.println(("CURRENT PIECE: " +this.teamColor));
                System.out.println("SELECTED PIECE: " + selectedPiece.getTeamColor());
            }

            if (selectedPiece == null) {
                System.out.println("ADDED MOVE - POS ROW: " + row + " POS COL: " + col);

                this.possibleMoves.add(new ChessMove(currentPosition, finalPosition, null));

                return true;
            } else {
                boolean killPiece = !Objects.equals(selectedPiece.getTeamColor(), this.teamColor);

                if (killPiece) {
                    System.out.println("ADDED MOVE - POS ROW: " + row + " POS COL: " + col);
                    this.possibleMoves.add(new ChessMove(currentPosition, finalPosition, null));
                }

                return false;
            }
        }

        return false;
    }

    private void diagonalCheck(ChessBoard board, ChessPosition myPosition) {
        incrementerCheck(INCREMENT_POSITIVE, INCREMENT_POSITIVE, board, myPosition);
        System.out.println("CHECK 2");
        incrementerCheck(INCREMENT_POSITIVE, INCREMENT_NEGATIVE, board, myPosition);
        System.out.println("CHECK 3");
        incrementerCheck(INCREMENT_NEGATIVE, INCREMENT_POSITIVE, board, myPosition);
        incrementerCheck(INCREMENT_NEGATIVE, INCREMENT_NEGATIVE, board, myPosition);
    }

    private void horizontalVerticalCheck(ChessBoard board, ChessPosition myPosition) {
        incrementerCheck(NO_INCREMENT, INCREMENT_POSITIVE, board, myPosition);
        incrementerCheck(NO_INCREMENT, INCREMENT_NEGATIVE, board, myPosition);
        incrementerCheck(INCREMENT_POSITIVE, NO_INCREMENT, board, myPosition);
        incrementerCheck(INCREMENT_NEGATIVE, NO_INCREMENT, board, myPosition);
    }

    private void knightCheck(ChessBoard board, ChessPosition myPosition) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        System.out.println("KNIGHT POSITION - ROW: " + row + " COL: " + col);

        validationCheck(row + KNIGHT_PRIMARY_MOVEMENT, col + KNIGHT_SECONDARY_MOVEMENT, board, myPosition);
        validationCheck(row + KNIGHT_PRIMARY_MOVEMENT, col - KNIGHT_SECONDARY_MOVEMENT, board, myPosition);
        validationCheck(row - KNIGHT_PRIMARY_MOVEMENT, col + KNIGHT_SECONDARY_MOVEMENT, board, myPosition);
        validationCheck(row - KNIGHT_PRIMARY_MOVEMENT, col - KNIGHT_SECONDARY_MOVEMENT, board, myPosition);
        validationCheck(row + KNIGHT_SECONDARY_MOVEMENT, col + KNIGHT_PRIMARY_MOVEMENT, board, myPosition);
        validationCheck(row - KNIGHT_SECONDARY_MOVEMENT, col + KNIGHT_PRIMARY_MOVEMENT, board, myPosition);
        validationCheck(row + KNIGHT_SECONDARY_MOVEMENT, col - KNIGHT_PRIMARY_MOVEMENT, board, myPosition);
        validationCheck(row - KNIGHT_SECONDARY_MOVEMENT, col - KNIGHT_PRIMARY_MOVEMENT, board, myPosition);
    }

    private void kingCheck(ChessBoard board, ChessPosition myPosition) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        validationCheck(row, col + INCREMENT_POSITIVE, board, myPosition);
        validationCheck(row, col + INCREMENT_NEGATIVE, board, myPosition);
        validationCheck(row + INCREMENT_POSITIVE, col, board, myPosition);
        validationCheck(row + INCREMENT_NEGATIVE, col, board, myPosition);
        validationCheck(row + INCREMENT_POSITIVE, col + INCREMENT_POSITIVE, board, myPosition);
        validationCheck(row + INCREMENT_POSITIVE, col + INCREMENT_NEGATIVE, board, myPosition);
        validationCheck(row + INCREMENT_NEGATIVE, col + INCREMENT_POSITIVE, board, myPosition);
        validationCheck(row + INCREMENT_NEGATIVE, col + INCREMENT_NEGATIVE, board, myPosition);
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        this.possibleMoves.clear();

        switch (this.pieceType) {
            case PieceType.KING:
                kingCheck(board, myPosition);

                break;

            case PieceType.KNIGHT:
                knightCheck(board, myPosition);
                break;

            case PieceType.PAWN:

                break;

            case PieceType.BISHOP:
                diagonalCheck(board, myPosition);

                break;

            case PieceType.ROOK:
                horizontalVerticalCheck(board, myPosition);

                break;

            case PieceType.QUEEN:
                diagonalCheck(board, myPosition);
                horizontalVerticalCheck(board, myPosition);

                break;


        }
        return this.possibleMoves;
    }

}
