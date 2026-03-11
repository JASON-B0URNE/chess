package client;

import chess.*;

import java.lang.reflect.Field;
import java.util.*;

import static ui.EscapeSequences.*;

public class ClientMain {
    private static String status = "LOGGED_OUT";
    private static boolean quit = false;
    private static ChessBoard chessBoard = new ChessBoard();

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

    private static void printBoard(ChessBoard board, String perspective) {
        List<Character> cols = Arrays.asList('h','g','f','e','d','c','b','a');
        List<Integer> rows = Arrays.asList(7,6,5,4,3,2,1,0);

        if (Objects.equals(perspective, "BLACK")) {
            Collections.reverse(cols);
            Collections.reverse(rows);
        }

        printRowHeader(cols);
        for (int row : rows) {
            printSquare(null, String.valueOf(row + 1));
            for (int col : rows) {
                String bgColor;
                if (row % 2 > 0) {
                    if (col % 2 > 0) {
                        bgColor = SET_BG_COLOR_LIGHT_GREY;
                    } else {
                        bgColor = SET_BG_COLOR_DARK_GREY;
                    }
                } else {
                    if (col % 2 > 0) {
                        bgColor = SET_BG_COLOR_DARK_GREY;
                    } else {
                        bgColor = SET_BG_COLOR_LIGHT_GREY;
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
            printSquare(null, String.valueOf(row));
            System.out.print("\n");
        }
        printRowHeader(cols);
    }

    private static void invalidCommand() {
        System.out.print(SET_TEXT_COLOR_RED + "ERROR: Invalid command." + RESET_TEXT_COLOR + "\n\n");
        help();
    }

    private static void notAuthorizedCheck() {
        System.out.print(SET_TEXT_COLOR_RED + "ERROR: Not authorized. Please login first." + RESET_TEXT_COLOR + "\n");
    }


    private static void parseCommand(String line) {
        var args = line.split(" ");

        ArrayList<String> commandList = new ArrayList<>(Arrays.asList(args));

        String command = commandList.getFirst().toLowerCase();

        if (Objects.equals(command, "register")) {
            //TODO: REGISTER
        } else if (Objects.equals(command, "login")) {
            //TODO: LOGIN
        } else if (Objects.equals(command, "quit")) {
            quit = true;
            //TODO: QUIT
        } else if (Objects.equals(command, "help")) {
            help();
        } else if (Objects.equals(command, "create")) {
            if (Objects.equals(status, "LOGGED_OUT")) {
                notAuthorizedCheck();
                return;
            }
        } else if (Objects.equals(command, "list")) {
            if (Objects.equals(status, "LOGGED_OUT")) {
                notAuthorizedCheck();
                return;
            }
        } else if (Objects.equals(command, "join")) {
            if (Objects.equals(status, "LOGGED_OUT")) {
                notAuthorizedCheck();
                return;
            }

            printBoard(chessBoard, "BLACK");
        } else if (Objects.equals(command, "observe")) {
            if (Objects.equals(status, "LOGGED_OUT")) {
                notAuthorizedCheck();
                return;
            }

            printBoard(chessBoard, "WHITE");
        } else if (Objects.equals(command, "logout")) {

        } else {
            invalidCommand();
        }
    }

    private static void help() {
        if (Objects.equals(status, "LOGGED_IN")) {
            System.out.print(SET_TEXT_COLOR_BLUE + "create <NAME>" + RESET_TEXT_COLOR);
            System.out.print(" - a game\n");
            System.out.print(SET_TEXT_COLOR_BLUE + "list" + RESET_TEXT_COLOR);
            System.out.print(" - games\n");
            System.out.print(SET_TEXT_COLOR_BLUE + "join <ID> [WHITE|BLACK]" + RESET_TEXT_COLOR);
            System.out.print(" - a game\n");
            System.out.print(SET_TEXT_COLOR_BLUE + "observe <ID>" + RESET_TEXT_COLOR);
            System.out.print(" - a game\n");
            System.out.print(SET_TEXT_COLOR_BLUE + "logout" + RESET_TEXT_COLOR);
            System.out.print(" - when you are done\n");
            System.out.print(SET_TEXT_COLOR_BLUE + "quit" + RESET_TEXT_COLOR);
            System.out.print(" - playing chess\n");
            System.out.print(SET_TEXT_COLOR_BLUE + "help" + RESET_TEXT_COLOR);
            System.out.print(" - with possible commands\n");
        } else {
            System.out.print(SET_TEXT_COLOR_BLUE + "register <USERNAME> <PASSWORD> <EMAIL>" + RESET_TEXT_COLOR);
            System.out.print(" - to create an account\n");
            System.out.print(SET_TEXT_COLOR_BLUE + "login <USERNAME> <PASSWORD>" + RESET_TEXT_COLOR);
            System.out.print(" - to play chess\n");
            System.out.print(SET_TEXT_COLOR_BLUE + "quit" + RESET_TEXT_COLOR);
            System.out.print(" - playing chess\n");
            System.out.print(SET_TEXT_COLOR_BLUE + "help" + RESET_TEXT_COLOR);
            System.out.print(" - with possible commands\n");
        }
    }

    public static void main(String[] args) {
        chessBoard.resetBoard();
        ServerFacade facade = new ServerFacade(8080);
        System.out.println("♕ Welcome to 240 chess. Type Help to get started. ♕");
        System.out.print("\n");
        while (!quit) {
            System.out.print("[" + status + "] >>> ");
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            parseCommand(line);
            System.out.println();
        }
    }
}
