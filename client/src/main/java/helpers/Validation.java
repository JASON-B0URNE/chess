package helpers;

import chess.ChessPosition;

import java.util.Objects;

public class Validation {
    public static ChessPosition validatePosition(String position) {
        int col = 0;
        int row = 0;

        if (position.length() != 2) {
            return null;
        }

        char letter = position.toLowerCase().charAt(0);

        if (Objects.equals(letter, 'a')) {
            col = 1;
        } else if (Objects.equals(letter, 'b')) {
            col = 2;
        }  else if (Objects.equals(letter, 'c')) {
            col = 3;
        }  else if (Objects.equals(letter, 'd')) {
            col = 4;
        }  else if (Objects.equals(letter, 'e')) {
            col = 5;
        }  else if (Objects.equals(letter, 'f')) {
            col = 6;
        }  else if (Objects.equals(letter, 'g')) {
            col = 7;
        }  else if (Objects.equals(letter, 'h')) {
            col = 8;
        } else {
            return null;
        }

        if (Character.isDigit(position.charAt(1))) {
            row = Character.getNumericValue(position.charAt(1));
        } else {
            return null;
        }

        return new ChessPosition(row, col);
    }
}
