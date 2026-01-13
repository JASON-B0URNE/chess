package chess;

import java.util.Collection;
import java.util.Iterator;
import java.util.HashSet;

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


    private final ChessGame.TeamColor chessTeamColor;
    private final PieceType chessPieceType;
    private final Collection<ChessMove> possibleMoves = new HashSet<ChessMove>();;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.chessTeamColor = pieceColor;
        this.chessPieceType = type;
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
        return this.chessTeamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.chessPieceType;
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

    private void incrementerCheck(int incrementX, int incrementY, ChessBoard board, ChessPosition myPosition) {
        int position_y = myPosition.getRow();
        int position_x = myPosition.getColumn();

        ChessPosition currentPosition = new ChessPosition(position_x, position_y);

        position_x += incrementX;

        while (position_x < CHESS_BOARD_LENGTH && position_x > 0) {
            position_y += incrementY;

            if (! validationCheck(position_x, position_y, board, currentPosition)) {
                break;
            }

            position_x += incrementX;
        }

    }

    private boolean validationCheck(int position_x, int position_y, ChessBoard board, ChessPosition currentPosition) {
        if (position_y < CHESS_BOARD_LENGTH && position_y > 0) {
            ChessPiece selectedPiece = board.getPiece(new ChessPosition(position_x, position_y));

            if (selectedPiece == null || selectedPiece.getTeamColor() != this.chessTeamColor) {
                ChessPosition finalPosition = new ChessPosition(position_x, position_y);

                this.possibleMoves.add(new ChessMove(currentPosition, finalPosition, this.chessPieceType));

                return true;
            }
        }

        return false;
    }

    private void diagonalCheck(ChessBoard board, ChessPosition myPosition) {
        incrementerCheck(INCREMENT_POSITIVE, INCREMENT_POSITIVE, board, myPosition);
        incrementerCheck(INCREMENT_POSITIVE, INCREMENT_NEGATIVE, board, myPosition);
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
        int position_y = myPosition.getRow();
        int position_x = myPosition.getColumn();

        validationCheck(position_x + KNIGHT_PRIMARY_MOVEMENT, position_y + KNIGHT_SECONDARY_MOVEMENT, board, myPosition);
        validationCheck(position_x + KNIGHT_PRIMARY_MOVEMENT, position_y - KNIGHT_SECONDARY_MOVEMENT, board, myPosition);
        validationCheck(position_x - KNIGHT_PRIMARY_MOVEMENT, position_y + KNIGHT_SECONDARY_MOVEMENT, board, myPosition);
        validationCheck(position_x - KNIGHT_PRIMARY_MOVEMENT, position_y - KNIGHT_SECONDARY_MOVEMENT, board, myPosition);
        validationCheck(position_x + KNIGHT_SECONDARY_MOVEMENT, position_y + KNIGHT_PRIMARY_MOVEMENT, board, myPosition);
        validationCheck(position_x - KNIGHT_SECONDARY_MOVEMENT, position_y + KNIGHT_PRIMARY_MOVEMENT, board, myPosition);
        validationCheck(position_x + KNIGHT_SECONDARY_MOVEMENT, position_y - KNIGHT_PRIMARY_MOVEMENT, board, myPosition);
        validationCheck(position_x - KNIGHT_SECONDARY_MOVEMENT, position_y - KNIGHT_PRIMARY_MOVEMENT, board, myPosition);
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        this.possibleMoves.clear();

        switch (this.chessPieceType) {
            case PieceType.KING:

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
