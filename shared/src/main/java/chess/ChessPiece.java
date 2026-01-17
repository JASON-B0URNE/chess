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
    private static final int FIRST_ROW_INDEX = 1;
    private static final int LAST_ROW_INDEX = 8;
    private static final int PAWN_SPECIAL_MOVE = 2;

    private final ChessGame.TeamColor teamColor;
    private final PieceType pieceType;
    private final Collection<ChessMove> possibleMoves = new HashSet<ChessMove>();;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
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

    private void incrementerCheck(int incrementRow, int incrementCol, ChessBoard board, ChessPosition myPosition) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        row += incrementRow;

        while (row <= CHESS_BOARD_LENGTH && row > 0) {
            col += incrementCol;

            if (! validationCheck(row, col, board, myPosition, null, true)) {
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

    private boolean validationCheck(int row, int col, ChessBoard board, ChessPosition currentPosition, PieceType promotion, boolean killDirective) {
        if (col <= CHESS_BOARD_LENGTH && col > 0 && row <= CHESS_BOARD_LENGTH && row > 0) {
            ChessPosition finalPosition = new ChessPosition(row, col);
            ChessPiece selectedPiece = board.getPiece(finalPosition);

            if (selectedPiece == null && Objects.equals(PieceType.PAWN, this.pieceType) && killDirective) {
                return false;
            }

            if (selectedPiece == null) {
                this.possibleMoves.add(new ChessMove(currentPosition, finalPosition, promotion));

                return true;
            } else {
                boolean killPiece = !Objects.equals(selectedPiece.getTeamColor(), this.teamColor);

                if (killPiece && killDirective) {
                    this.possibleMoves.add(new ChessMove(currentPosition, finalPosition, promotion));
                }

                return false;
            }
        }

        return false;
    }

    private void directionalCheck(boolean diagonal, boolean straight, ChessBoard board, ChessPosition myPosition) {
        if (diagonal) {
            incrementerCheck(INCREMENT_POSITIVE, INCREMENT_POSITIVE, board, myPosition);
            incrementerCheck(INCREMENT_POSITIVE, INCREMENT_NEGATIVE, board, myPosition);
            incrementerCheck(INCREMENT_NEGATIVE, INCREMENT_POSITIVE, board, myPosition);
            incrementerCheck(INCREMENT_NEGATIVE, INCREMENT_NEGATIVE, board, myPosition);
        }
        if (straight) {
            incrementerCheck(NO_INCREMENT, INCREMENT_POSITIVE, board, myPosition);
            incrementerCheck(NO_INCREMENT, INCREMENT_NEGATIVE, board, myPosition);
            incrementerCheck(INCREMENT_POSITIVE, NO_INCREMENT, board, myPosition);
            incrementerCheck(INCREMENT_NEGATIVE, NO_INCREMENT, board, myPosition);
        }
    }

    private void knightCheck(ChessBoard board, ChessPosition myPosition) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        validationCheck(row + KNIGHT_PRIMARY_MOVEMENT, col + KNIGHT_SECONDARY_MOVEMENT, board, myPosition, null,true);
        validationCheck(row + KNIGHT_PRIMARY_MOVEMENT, col - KNIGHT_SECONDARY_MOVEMENT, board, myPosition, null,true);
        validationCheck(row - KNIGHT_PRIMARY_MOVEMENT, col + KNIGHT_SECONDARY_MOVEMENT, board, myPosition, null,true);
        validationCheck(row - KNIGHT_PRIMARY_MOVEMENT, col - KNIGHT_SECONDARY_MOVEMENT, board, myPosition, null, true);
        validationCheck(row + KNIGHT_SECONDARY_MOVEMENT, col + KNIGHT_PRIMARY_MOVEMENT, board, myPosition, null, true);
        validationCheck(row - KNIGHT_SECONDARY_MOVEMENT, col + KNIGHT_PRIMARY_MOVEMENT, board, myPosition, null, true);
        validationCheck(row + KNIGHT_SECONDARY_MOVEMENT, col - KNIGHT_PRIMARY_MOVEMENT, board, myPosition, null, true);
        validationCheck(row - KNIGHT_SECONDARY_MOVEMENT, col - KNIGHT_PRIMARY_MOVEMENT, board, myPosition, null, true);
    }

    private void kingCheck(ChessBoard board, ChessPosition myPosition) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        validationCheck(row, col + INCREMENT_POSITIVE, board, myPosition, null, true);
        validationCheck(row, col + INCREMENT_NEGATIVE, board, myPosition, null, true);
        validationCheck(row + INCREMENT_POSITIVE, col, board, myPosition, null, true);
        validationCheck(row + INCREMENT_NEGATIVE, col, board, myPosition, null, true);
        validationCheck(row + INCREMENT_POSITIVE, col + INCREMENT_POSITIVE, board, myPosition, null, true);
        validationCheck(row + INCREMENT_POSITIVE, col + INCREMENT_NEGATIVE, board, myPosition, null, true);
        validationCheck(row + INCREMENT_NEGATIVE, col + INCREMENT_POSITIVE, board, myPosition, null, true);
        validationCheck(row + INCREMENT_NEGATIVE, col + INCREMENT_NEGATIVE, board, myPosition, null, true);
    }

    private void pawnPromotion(int row, int col, int rowIncrementer, ChessBoard board, ChessPosition myPosition) {
        pawnPossibleMoves(row, col, rowIncrementer, board, myPosition, PieceType.QUEEN);
        pawnPossibleMoves(row, col, rowIncrementer, board, myPosition, PieceType.ROOK);
        pawnPossibleMoves(row, col, rowIncrementer, board, myPosition, PieceType.BISHOP);
        pawnPossibleMoves(row, col, rowIncrementer, board, myPosition, PieceType.KNIGHT);
    }

    private void pawnPossibleMoves(int row, int col, int rowIncrementer, ChessBoard board, ChessPosition myPosition, PieceType promotion) {
        validationCheck(row + rowIncrementer, col, board, myPosition, promotion, false);
        validationCheck(row + rowIncrementer, col + INCREMENT_POSITIVE, board, myPosition, promotion, true);
        validationCheck(row + rowIncrementer, col + INCREMENT_NEGATIVE, board, myPosition, promotion, true);
    }

    private boolean pawnSpecialMoves(int startRow, int endRow, int row, int col, int rowIncrementer, ChessBoard board, ChessPosition myPosition) {
        ChessPosition testPosition = new ChessPosition(row + rowIncrementer, col);
        ChessPiece selectedPiece = board.getPiece(testPosition);

        if (row == startRow) {
            if (selectedPiece == null) {
                validationCheck(row + (rowIncrementer * PAWN_SPECIAL_MOVE), col, board, myPosition, null, false);
            }
        } else if (row == endRow) {
            pawnPromotion(row, col, rowIncrementer, board, myPosition);
            return false;
        }
        return true;
    }

    private void pawnCheck(ChessBoard board, ChessPosition myPosition) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        int rowIncrementer;
        boolean continueMoves;

        if (Objects.equals(teamColor, ChessGame.TeamColor.WHITE)) {
            rowIncrementer = INCREMENT_POSITIVE;
            continueMoves = pawnSpecialMoves(2,7,row,col, rowIncrementer, board, myPosition);
        } else {
            rowIncrementer = INCREMENT_NEGATIVE;
            continueMoves = pawnSpecialMoves(7,2,row,col, rowIncrementer, board, myPosition);
        }

        if (continueMoves) {
            pawnPossibleMoves(row, col, rowIncrementer, board, myPosition, null);
        }
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
                pawnCheck(board, myPosition);

                break;

            case PieceType.BISHOP:
                directionalCheck(true, false, board, myPosition);

                break;

            case PieceType.ROOK:
                directionalCheck(false, true, board, myPosition);

                break;

            case PieceType.QUEEN:
                directionalCheck(true, true, board, myPosition);

                break;


        }
        return this.possibleMoves;
    }

}
