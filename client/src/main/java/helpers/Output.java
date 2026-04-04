package helpers;

import java.util.Objects;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.RESET_TEXT_COLOR;
import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;
import static ui.EscapeSequences.SET_TEXT_COLOR_GREEN;
import static ui.EscapeSequences.SET_TEXT_COLOR_MAGENTA;
import static ui.EscapeSequences.SET_TEXT_COLOR_RED;

public class Output {
    static String status;
    static String gameStatus;

    public Output(String status, String gameStatus) {
        Output.status = status;
        Output.gameStatus = gameStatus;
    }

    public static void help() {
        if (Objects.equals(status, "LOGGED_IN")) {
            if (Objects.equals(gameStatus,"PLAYING")) {
                System.out.print(SET_TEXT_COLOR_BLUE + "redraw" + RESET_TEXT_COLOR);
                System.out.print(" - chess board\n");
                System.out.print(SET_TEXT_COLOR_BLUE + "move <POSITION> <POSITION> (OPTIONAL PROMOTION TYPE - QUEEN|ROOK|KNIGHT|BISHOP)" + RESET_TEXT_COLOR);
                System.out.print(" - chess pieces\n");
                System.out.print(SET_TEXT_COLOR_MAGENTA);
                System.out.print("EXAMPLES:\nmove a8 a6 - move a standard piece from a8 to a6\nmove c7 c8 QUEEN - move a pawn from c7 to c8 and promote it to QUEEN\n");
                System.out.print(RESET_TEXT_COLOR);
                System.out.print(SET_TEXT_COLOR_BLUE + "resign" + RESET_TEXT_COLOR);
                System.out.print(" - chess match\n");
                System.out.print(SET_TEXT_COLOR_BLUE + "highlight <POSITION>" + RESET_TEXT_COLOR);
                System.out.print(" - legal chess moves\n");
                System.out.print(SET_TEXT_COLOR_MAGENTA);
                System.out.print("EXAMPLE:\nhighlight e4 - displays all legal moves for the piece at e4\n");
                System.out.print(RESET_TEXT_COLOR);
                System.out.print(SET_TEXT_COLOR_BLUE + "help" + RESET_TEXT_COLOR);
                System.out.print(" - with possible commands\n");
                return;
            } else if (Objects.equals(gameStatus, "OBSERVING")) {
                System.out.print(SET_TEXT_COLOR_BLUE + "redraw" + RESET_TEXT_COLOR);
                System.out.print(" - chess board\n");
                System.out.print(SET_TEXT_COLOR_BLUE + "leave" + RESET_TEXT_COLOR);
                System.out.print(" - current observation\n");
                System.out.print(SET_TEXT_COLOR_BLUE + "help" + RESET_TEXT_COLOR);
                System.out.print(" - with possible commands\n");
                return;
            }

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

    public static void invalidCommand() {
        System.out.print(SET_TEXT_COLOR_RED + "ERROR: Invalid command." + RESET_TEXT_COLOR + "\n\n");
        help();
    }

    public static void notAuthorizedCheck() {
        System.out.print(SET_TEXT_COLOR_RED + "ERROR: Not authorized. Please login first." + RESET_TEXT_COLOR + "\n");
    }

    public static void handleErrors(int code) {
        System.out.print(SET_TEXT_COLOR_RED);
        if (Objects.equals(code, 500)) {
            System.out.print("ERROR: Database error.");
        } else if (Objects.equals(code, 400)) {
            System.out.print("ERROR: Bad request.");
        } else if (Objects.equals(code, 401)) {
            System.out.print("ERROR: Unauthorized request.");
        } else if (Objects.equals(code, 403)) {
            System.out.print("ERROR: Item already exists in database.");
        }
        System.out.print(RESET_TEXT_COLOR + "\n");
    }

    public static void printSuccess(String message) {
        System.out.print(SET_TEXT_COLOR_GREEN);
        System.out.print("SUCCESS: " + message);
        System.out.print(RESET_TEXT_COLOR + "\n");
    }
}
