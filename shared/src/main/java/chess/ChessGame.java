package chess;

import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor currentTeam;
    private ChessBoard board = new ChessBoard();
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
//            Collection<ChessMove> safeTeamMoves = kingCheck(getTeamTurn());
            Collection<ChessMove> possibleMoves = new HashSet<>(selectedPiece.pieceMoves(this.board, startPosition));
//            possibleMoves.removeIf(x -> !safeTeamMoves.contains(x));
            return possibleMoves;
        } else {
            return null;
        }
    }

    public void movement(ChessMove move) {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();

        ChessPiece selectedPiece = this.board.getPiece(startPosition);

        if (move.getPromotionPiece() != null) {
            selectedPiece = new ChessPiece(selectedPiece.getTeamColor(), move.getPromotionPiece());
        }

        this.board.addPiece(startPosition, null);
        this.board.addPiece(endPosition, selectedPiece);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPiece selectedPiece = this.board.getPiece(startPosition);
        Collection<ChessMove> validMoves = validMoves(startPosition);

        Collection<ChessMove> safeTeamMoves = kingCheck(getTeamTurn());

        if (validMoves == null || !validMoves.contains(move) || (!Objects.equals(selectedPiece.getTeamColor(), this.currentTeam))) {
            throw new InvalidMoveException("Invalid Move");
        }

        if (safeTeamMoves.contains(move)) {
            movement(move);
        }

        if (this.currentTeam == TeamColor.BLACK) {
            this.currentTeam = TeamColor.WHITE;
        } else {
            this.currentTeam = TeamColor.BLACK;
        }
    }

    private CheckResult generateMoves(TeamColor teamColor) {
        Collection<ChessMove>enemyMoves = new HashSet<>();
        ChessPosition kingPosition = null;
        Collection<ChessPosition>enemyPositions = new HashSet<>();
        Collection<ChessMove>teamMoves = new HashSet<>();

        for (int row = 1; row <= CHESS_BOARD_LENGTH; row++) {
            for (int col = 1; col <= CHESS_BOARD_LENGTH; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece selectedPiece = this.board.getPiece(position);

                if (selectedPiece == null) {
                    continue;
                }

                if (!Objects.equals(selectedPiece.getTeamColor(), teamColor)) {
                    if (Objects.equals(selectedPiece.getPieceType(), ChessPiece.PieceType.PAWN)) {
                        Collection<ChessMove> pawnMoves = this.board.getPiece(position).pieceMoves(this.board, position);
                        pawnMoves.removeIf(x ->
                                x.getStartPosition().getRow() + 1 == x.getEndPosition().getRow() ||
                                        x.getStartPosition().getRow() - 1 == x.getEndPosition().getRow()
                        );
                        enemyMoves.addAll(pawnMoves);
                        continue;
                    }
                    enemyMoves.addAll(
                            this.board.getPiece(position).pieceMoves(this.board, position)
                    );
                } else {
                    if (Objects.equals(selectedPiece.getPieceType(), ChessPiece.PieceType.KING)) {
                        kingPosition = position;
                    } else {
                        teamMoves.addAll(
                                this.board.getPiece(position).pieceMoves(this.board, position)
                        );
                    }
                }
            }
        }

        for (ChessMove move : enemyMoves) {
            enemyPositions.add(move.getEndPosition());
        }

        return new CheckResult(enemyPositions, kingPosition, enemyMoves, teamMoves);
    }

    private Collection<ChessMove> kingCheck(TeamColor teamColor) {
        CheckResult result = generateMoves(teamColor);
        Collection<ChessMove> teamMoves = new ArrayList<>(result.teamMoves);

        if (!result.enemyPositions.contains(result.kingPosition)) {
            return result.teamMoves;
        }

        ChessBoard copyBoard = getBoard().clone();

        Collection<ChessMove> safeMoves = new HashSet<>();

        teamMoves.forEach(x -> {
            System.out.println("What?");

            movement(x);

            if (!isInCheck(teamColor)) {
                safeMoves.add(x);
            }

            this.board = copyBoard;
        });

        return safeMoves;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        CheckResult result = generateMoves(teamColor);

        return result.enemyPositions.contains(result.kingPosition);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        Collection<ChessMove> safeTeamMoves = kingCheck(teamColor);

        return safeTeamMoves.isEmpty();
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        CheckResult result = generateMoves(teamColor);

        return result.teamMoves.isEmpty();
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

    private class CheckResult {
        public Collection<ChessPosition>enemyPositions = new HashSet<>();
        public ChessPosition kingPosition = null;
        public Collection<ChessMove>enemyMoves = new HashSet<>();
        public Collection<ChessMove>teamMoves = new HashSet<>();

        public CheckResult(Collection<ChessPosition> pos1, ChessPosition pos2, Collection<ChessMove> pos3, Collection<ChessMove> pos4) {
            this.enemyPositions = pos1;
            this.kingPosition = pos2;
            this.enemyMoves = pos3;
            this.teamMoves = pos4;
        }
    }

    private class CheckmateResult {
        public Collection<ChessMove>safeKingMoves = new HashSet<>();
        public Collection<ChessMove>safeTeamMoves = new HashSet<>();

        public CheckmateResult(Collection<ChessMove> pos1, Collection<ChessMove> pos2) {
            this.safeKingMoves = pos1;
            this.safeTeamMoves = pos2;
        }
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