package client;

import chess.*;
import com.google.gson.Gson;
import model.AuthData;
import requests.Response;

import java.lang.reflect.Field;
import java.util.*;

import static ui.EscapeSequences.*;

public class ClientMain {
    private static String status = "LOGGED_OUT";
    private static boolean quit = false;
    private static ChessBoard chessBoard = new ChessBoard();
    private static ServerFacade facade;
    private static AuthData session;
    private static Collection<Map<String, String>> listGames;

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

    private static void handleErrors(int code) {
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

    private static void printSuccess(String message) {
        System.out.print(SET_TEXT_COLOR_GREEN);
        System.out.print("SUCCESS: " + message);
        System.out.print(RESET_TEXT_COLOR + "\n");
    }

    private static void clearDatabase() {
        try {
            Response response = facade.clear();
            if (!Objects.equals(response.code(), 200)) {
                handleErrors(response.code());
            } else {
                printSuccess("Database cleared successfully.");
            }
        } catch (Exception ex) {
            System.out.print(SET_TEXT_COLOR_RED + "ERROR: Server Error." + RESET_TEXT_COLOR + "\n");
        }
    }

    private void parseCommand(String line) {
        var args = line.split(" ");
        var serializer = new Gson();

        ArrayList<String> commandList = new ArrayList<>(Arrays.asList(args));

        String command = commandList.getFirst().toLowerCase();

        if (Objects.equals(command, "register")) {
            if (!Objects.equals(commandList.size(), 4) ||
                    Objects.equals(status, "LOGGED_IN")) {
                invalidCommand();
                return;
            }
            try {
                Response response = facade.createUser(commandList.get(1), commandList.get(2), commandList.get(3));
                if (!Objects.equals(response.code(), 200)) {
                    handleErrors(response.code());
                } else {
                    session = serializer.fromJson(response.json(), AuthData.class);
                    printSuccess("User was created successfully.");
                    status = "LOGGED_IN";
                }
            } catch (Exception ex) {
                System.out.print(SET_TEXT_COLOR_RED + "ERROR: Server Error." + RESET_TEXT_COLOR + "\n");
            }
        } else if (Objects.equals(command, "login")) {
            if (!Objects.equals(commandList.size(), 3) ||
                    Objects.equals(status, "LOGGED_IN")) {
                invalidCommand();
                return;
            }
            try {
                Response response = facade.createSession(commandList.get(1), commandList.get(2));
                if (!Objects.equals(response.code(), 200)) {
                    handleErrors(response.code());
                } else {
                    session = serializer.fromJson(response.json(), AuthData.class);
                    printSuccess("Successful login.");
                    status = "LOGGED_IN";
                }
            } catch (Exception ex) {
                System.out.print(SET_TEXT_COLOR_RED + "ERROR: Server Error." + RESET_TEXT_COLOR + "\n");
            }
        } else if (Objects.equals(command, "quit")) {
            if (!Objects.equals(commandList.size(), 1)) {
                invalidCommand();
                return;
            }
            quit = true;
            if (Objects.equals(status, "LOGGED_IN")) {
                try {
                    Response response = facade.deleteSession(session.authToken());
                    if (!Objects.equals(response.code(), 200)) {
                        handleErrors(response.code());
                    } else {
                        session = serializer.fromJson(response.json(), AuthData.class);
                        status = "LOGGED_OUT";
                    }
                } catch (Exception ex) {
                    System.out.print(SET_TEXT_COLOR_RED + "ERROR: Server Error." + RESET_TEXT_COLOR + "\n");
                }
            }
        } else if (Objects.equals(command, "help")) {
            if (!Objects.equals(commandList.size(), 1)) {
                invalidCommand();
                return;
            }

            help();
        } else if (Objects.equals(command, "create")) {
            if (!Objects.equals(commandList.size(), 2)) {
                invalidCommand();
                return;
            }
            if (Objects.equals(status, "LOGGED_OUT")) {
                notAuthorizedCheck();
                return;
            }
            try {
                Response response = facade.createGame(session.authToken(), commandList.get(1));
                if (!Objects.equals(response.code(), 200)) {
                    handleErrors(response.code());
                } else {
                    Map<String, Number> gamesResponse = serializer.fromJson(response.json(), Map.class);
                    Number gameID = gamesResponse.get("gameID");
                    printSuccess("The game was successfully created.\nGame ID: " + gameID.intValue());
                }
            } catch (Exception ex) {
                System.out.print(SET_TEXT_COLOR_RED + "ERROR: Server Error." + RESET_TEXT_COLOR + "\n");
            }
        } else if (Objects.equals(command, "list")) {
            if (!Objects.equals(commandList.size(), 1)) {
                invalidCommand();
                return;
            }
            if (Objects.equals(status, "LOGGED_OUT")) {
                notAuthorizedCheck();
                return;
            }
            try {
                Response response = facade.getGames(session.authToken());
                if (!Objects.equals(response.code(), 200)) {
                    handleErrors(response.code());
                } else {
                    Map<String, Collection<Map<String, String>>> gamesResponse = serializer.fromJson(response.json(), Map.class);
                    Collection<Map<String, String>> games = gamesResponse.get("games");
                    listGames = games;
                    System.out.print(SET_TEXT_COLOR_MAGENTA);
                    System.out.printf("%-10s %-20s %-20s %-20s%n",
                            "Game ID:", "Game Name:", "White:", "Black:");
                    System.out.print(RESET_TEXT_COLOR);
                    for (var game : games) {

                        String white;
                        String black;
                        if (game.get("whiteUsername") == null) {
                            white = SET_TEXT_COLOR_GREEN + String.format("%-20s", "AVAILABLE") + RESET_TEXT_COLOR;
                        } else {
                            white = SET_TEXT_COLOR_RED + String.format("%-20s", game.get("whiteUsername")) + RESET_TEXT_COLOR;
                        }
                        if (game.get("blackUsername") == null) {
                            black = SET_TEXT_COLOR_GREEN + String.format("%-20s", "AVAILABLE") + RESET_TEXT_COLOR;
                        } else {
                            black = SET_TEXT_COLOR_RED + String.format("%-20s", game.get("blackUsername")) + RESET_TEXT_COLOR;
                        }

                        Object parse = game.get("gameID");
                        int gameID = ((Number) parse).intValue();

                        System.out.printf("%-10s %-20s %s %s %n",
                                gameID, game.get("gameName"), white, black);
                    }
                }
            } catch (Exception ex) {
                System.out.print(SET_TEXT_COLOR_RED + "ERROR: Server Error." + RESET_TEXT_COLOR + "\n");
            }
        } else if (Objects.equals(command, "join")) {
            if (!Objects.equals(commandList.size(), 3) ||
                    !(Objects.equals(commandList.get(2), "WHITE") || Objects.equals(commandList.get(2), "BLACK"))) {
                invalidCommand();
                return;
            }
            if (Objects.equals(status, "LOGGED_OUT")) {
                notAuthorizedCheck();
                return;
            }
            try {
                int gameID = Integer.parseInt(commandList.get(1));
                Response response = facade.joinGame(session.authToken(), commandList.get(2), gameID);
                if (!Objects.equals(response.code(), 200)) {
                    handleErrors(response.code());
                } else {
                    printBoard(chessBoard, "BLACK");
                }
            } catch (Exception e) {
                invalidCommand();
            }
        } else if (Objects.equals(command, "observe")) {
            if (!Objects.equals(commandList.size(), 2) ||
                    !(Objects.equals(commandList.get(2), "WHITE") || Objects.equals(commandList.get(2), "BLACK"))) {
                invalidCommand();
                return;
            }
            if(listGames.isEmpty()) {
                System.out.print(SET_TEXT_COLOR_RED + "ERROR: No games have been listed. Run the list command." + RESET_TEXT_COLOR + "\n");
            }

            if (Objects.equals(status, "LOGGED_OUT")) {
                notAuthorizedCheck();
                return;
            }

            try {
                int gameID = Integer.parseInt(commandList.get(1));

                if (gameID > listGames.size()) {
                    System.out.print(SET_TEXT_COLOR_RED + "ERROR: Game ID does not exist." + RESET_TEXT_COLOR + "\n");
                    return;
                }

                printBoard(chessBoard, "WHITE");
            } catch (Exception e) {
                invalidCommand();
            }
        } else if (Objects.equals(command, "logout")) {
            if (!Objects.equals(commandList.size(), 1)) {
                invalidCommand();
                return;
            }
            if (Objects.equals(status, "LOGGED_OUT")) {
                notAuthorizedCheck();
                return;
            }
            try {
                Response response = facade.deleteSession(session.authToken());
                if (!Objects.equals(response.code(), 200)) {
                    handleErrors(response.code());
                } else {
                    session = serializer.fromJson(response.json(), AuthData.class);
                    status = "LOGGED_OUT";
                }
            } catch (Exception ex) {
                System.out.print(SET_TEXT_COLOR_RED + "ERROR: Server Error." + RESET_TEXT_COLOR + "\n");
            }
        } else if (Objects.equals(command, "clear")) {
            clearDatabase();
        } else {
            invalidCommand();
        }
    }

    public void main(String[] args) {
        chessBoard.resetBoard();
        facade = new ServerFacade(8080);
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
