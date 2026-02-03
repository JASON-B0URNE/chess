package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor currentTeam;
    private ChessBoard board = new ChessBoard();
    private boolean inCheck;
    private boolean inCheckmate;
    private static final int CHESS_BOARD_LENGTH = 8;

    public ChessGame() {
        this.currentTeam = TeamColor.WHITE;
        this.board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.currentTeam;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTeam = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece selectedPiece = this.board.getPiece(startPosition);

        if (selectedPiece != null) {
            return selectedPiece.pieceMoves(this.board, startPosition);
        } else {
            return null;
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();

        ChessPiece selectedPiece = this.board.getPiece(startPosition);

        Collection<ChessMove> validMoves = validMoves(startPosition);

        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid Move");
        }

        if (move.getPromotionPiece() != null) {
            selectedPiece = new ChessPiece(selectedPiece.getTeamColor(), move.getPromotionPiece());
        }

        this.board.addPiece(startPosition, null);
        this.board.addPiece(endPosition, selectedPiece);
    }

    private void kingCheck(TeamColor teamcolor) {
        ChessPosition kingPosition = null;
        Collection<ChessMove> enemyMoves = new HashSet<ChessMove>();
        Collection<ChessPosition> enemyMovePosition = new HashSet<>();
        Collection<ChessMove> kingMoves = new HashSet<>();
        Collection<ChessPosition> kingMovePosition = new HashSet<>();

        for (int row = 0; row < CHESS_BOARD_LENGTH; row++) {
            for(int col = 0; col < CHESS_BOARD_LENGTH; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece selectedPiece = this.board.getPiece(position);

                if (selectedPiece.getTeamColor() != teamcolor) {
                    enemyMoves.addAll(selectedPiece.pieceMoves(this.board, position));
                } else {
                    if (selectedPiece.getPieceType() == ChessPiece.PieceType.KING) {
                        kingPosition = position;
                    }
                }
            }
        }
        for (ChessMove move : enemyMoves) {
            enemyMovePosition.add(move.getEndPosition());
        }

        kingMoves = this.board.getPiece(kingPosition).pieceMoves(this.board, kingPosition);
        for (ChessMove move : kingMoves) {
            kingMovePosition.add(move.getEndPosition());
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return inCheck;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return currentTeam == chessGame.currentTeam && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTeam, board);
    }
}