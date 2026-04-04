package helpers;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.lang.reflect.Field;
import java.util.*;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.EMPTY;
import static ui.EscapeSequences.SET_BG_COLOR_DARK_GREEN;
import static ui.EscapeSequences.SET_BG_COLOR_DARK_GREY;
import static ui.EscapeSequences.SET_BG_COLOR_GREEN;
import static ui.EscapeSequences.SET_BG_COLOR_LIGHT_GREY;

public class PrintBoard {
    private static void printSquare(String bgColor, String character) {
        if (bgColor != null) {
            System.out.print(bgColor);
        }
        System.out.print(" " + character + " ");
        if (bgColor != null) {
            System.out.print(RESET_BG_COLOR);
        }
    }

    private static void printRowHeader(List<Character> cols) {
        printSquare(null, EMPTY);
        for (char col : cols) {
            System.out.print("\u2007" + col + EMPTY + "\u200A");
        }
        printSquare(null, EMPTY);
        System.out.print("\n");
    }

    public static void printBoard(ChessBoard board, ChessGame.TeamColor perspective, Collection<ChessPosition> tiles) {
        List<Character> cols = Arrays.asList('a','b','c','d','e','f','g','h');
        List<Integer> rows = Arrays.asList(7,6,5,4,3,2,1,0);
        List<Integer> reverseRows = Arrays.asList(0,1,2,3,4,5,6,7);

        if (Objects.equals(perspective, ChessGame.TeamColor.BLACK)) {
            Collections.reverse(cols);
            Collections.reverse(rows);
            Collections.reverse(reverseRows);
        }

        printRowHeader(cols);
        for (int row : rows) {
            printSquare(null, String.valueOf(row + 1));
            for (int col : reverseRows) {
                String bgColor;
                if (row % 2 > 0) {
                    if (col % 2 > 0) {
                        bgColor = SET_BG_COLOR_DARK_GREY;
                    } else {
                        bgColor = SET_BG_COLOR_LIGHT_GREY;
                    }
                } else {
                    if (col % 2 > 0) {
                        bgColor = SET_BG_COLOR_LIGHT_GREY;
                    } else {
                        bgColor = SET_BG_COLOR_DARK_GREY;
                    }
                }

                if (tiles != null) {
                    if (tiles.contains(new ChessPosition(row + 1, col + 1))) {
                        if (Objects.equals(bgColor, SET_BG_COLOR_LIGHT_GREY)) {
                            bgColor = SET_BG_COLOR_GREEN;
                        } else if (Objects.equals(bgColor, SET_BG_COLOR_DARK_GREY)) {
                            bgColor = SET_BG_COLOR_DARK_GREEN;
                        }
                    }
                }

                ChessPiece piece = board.getPiece(new ChessPosition(row + 1, col + 1));
                if (piece == null) {
                    printSquare(bgColor, EMPTY);
                } else {
                    String pieceType = piece.getTeamColor() + "_" + piece.getPieceType();
                    try {
                        Field field = ui.EscapeSequences.class.getField(pieceType);
                        String value = (String) field.get(null);
                        printSquare(bgColor, value);
                    } catch (NoSuchFieldException | IllegalAccessException ex) {
                    }
                }
            }
            printSquare(null, String.valueOf(row + 1));
            System.out.print("\n");
        }
        printRowHeader(cols);
    }
}
