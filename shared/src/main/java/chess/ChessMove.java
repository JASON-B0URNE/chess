package chess;

import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {
    private final ChessPosition chessStartPosition;
    private final ChessPosition chessEndPosition;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChessMove chessMove = (ChessMove) o;
        return Objects.equals(chessStartPosition, chessMove.chessStartPosition) && Objects.equals(chessEndPosition, chessMove.chessEndPosition) && chessPromotionPiece == chessMove.chessPromotionPiece;
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(chessStartPosition);
        result = 31 * result + Objects.hashCode(chessEndPosition);
        result = 31 * result + Objects.hashCode(chessPromotionPiece);
        return result;
    }

    private final ChessPiece.PieceType chessPromotionPiece;


    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.chessStartPosition = startPosition;
        this.chessEndPosition = endPosition;
        this.chessPromotionPiece = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return this.chessStartPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return this.chessEndPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return this.chessPromotionPiece;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s, %s]", "Start Position: (" + chessStartPosition+") ", "Start Position: (" + chessEndPosition + ") ","Promotion Piece: " + chessPromotionPiece);
    }
}
