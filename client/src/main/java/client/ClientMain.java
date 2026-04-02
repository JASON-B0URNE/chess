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
    private static String gameStatus = "OUT_OF_GAME";
    private static boolean quit = false;
    private static ChessGame chessGame = new ChessGame();
    private static ChessBoard chessBoard = chessGame.getBoard();
    private static ServerFacade facade;
    private static AuthData session;
    private static Collection<Map<String, String>> listGames;
    private static String color = "BLANK";

    private static void help() {
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

    private static void printBoard(ChessBoard board, String perspective, Collection<ChessPosition> tiles) {
        List<Character> cols = Arrays.asList('a','b','c','d','e','f','g','h');
        List<Integer> rows = Arrays.asList(7,6,5,4,3,2,1,0);
        List<Integer> reverseRows = Arrays.asList(0,1,2,3,4,5,6,7);

        if (Objects.equals(perspective, "BLACK")) {
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

    private void observe(ArrayList<String> commandList) {
        if (!Objects.equals(commandList.size(), 2) || !Objects.equals(gameStatus, "OUT_OF_GAME")) {
            invalidCommand();
            return;
        }
        if(listGames == null || listGames.isEmpty()) {
            System.out.print(SET_TEXT_COLOR_RED + "ERROR: No games have been listed. Run the list command." + RESET_TEXT_COLOR + "\n");
            return;
        }

        if (Objects.equals(status, "LOGGED_OUT")) {
            notAuthorizedCheck();
            return;
        }

        try {
            int gameID = Integer.parseInt(commandList.get(1));

            if (gameID > listGames.size() || gameID == 0) {
                System.out.print(SET_TEXT_COLOR_RED + "ERROR: Game ID does not exist." + RESET_TEXT_COLOR + "\n");
                return;
            }

            gameStatus = "OBSERVING";
            color = "WHITE";
            printBoard(chessBoard, color, null);
        } catch (Exception e) {
            invalidCommand();
        }
    }



    private void join(ArrayList<String> commandList) {
        if (!Objects.equals(commandList.size(), 3)
                || !(Objects.equals(commandList.get(2), "WHITE") || Objects.equals(commandList.get(2), "BLACK"))
                || !Objects.equals(gameStatus, "OUT_OF_GAME")) {
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
                gameStatus = "PLAYING";
                color = commandList.get(2);
                printBoard(chessBoard, color, null);
            }
        } catch (Exception e) {
            invalidCommand();
        }
    }



    private void deleteSession() {
        var serializer = new Gson();

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

    private void logout(ArrayList<String> commandList) {
        if (!Objects.equals(commandList.size(), 1) || !Objects.equals(gameStatus, "OUT_OF_GAME")) {
            invalidCommand();
            return;
        }
        if (Objects.equals(status, "LOGGED_OUT")) {
            notAuthorizedCheck();
            return;
        }

        deleteSession();
    }

    private void list(ArrayList<String> commandList) {
        var serializer = new Gson();

        if (!Objects.equals(commandList.size(), 1) || !Objects.equals(gameStatus, "OUT_OF_GAME")) {
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
    }

    private void create(ArrayList<String> commandList) {
        var serializer = new Gson();

        if (!Objects.equals(commandList.size(), 2) || !Objects.equals(gameStatus, "OUT_OF_GAME")) {
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
    }

    private void quit(ArrayList<String> commandList) {
        var serializer = new Gson();

        if (!Objects.equals(commandList.size(), 1) || !Objects.equals(gameStatus, "OUT_OF_GAME")) {
            invalidCommand();
            return;
        }
        quit = true;
        if (Objects.equals(status, "LOGGED_IN")) {
            deleteSession();
        }
    }

    private void login(ArrayList<String> commandList) {
        var serializer = new Gson();

        if (!Objects.equals(commandList.size(), 3) ||
                Objects.equals(status, "LOGGED_IN") || !Objects.equals(gameStatus, "OUT_OF_GAME")) {
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
    }

    private void register(ArrayList<String> commandList) {
        var serializer = new Gson();

        if (!Objects.equals(commandList.size(), 4) ||
                Objects.equals(status, "LOGGED_IN") || !Objects.equals(gameStatus, "OUT_OF_GAME")) {
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
    }

    private ChessPosition validatePosition(String position) {
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

    private void move(ArrayList<String> commandList) {
        if (commandList.size() < 3 || commandList.size() > 4 ||
                !Objects.equals(status, "LOGGED_IN") || !Objects.equals(gameStatus, "PLAYING")) {
            invalidCommand();
            return;
        }

        ChessPosition startPosition = validatePosition(commandList.get(1));
        ChessPosition endPosition = validatePosition(commandList.get(2));

        if (!Objects.equals(chessBoard.getPiece(startPosition).getTeamColor(), color)) {
            System.out.print(SET_TEXT_COLOR_RED + "ERROR: Cannot move pieces of the opposite color." + RESET_TEXT_COLOR + "\n");
            return;
        }

        if (startPosition == null || endPosition == null) {
            if (startPosition == null) {
                System.out.print(SET_TEXT_COLOR_RED + "ERROR: Invalid move start position." + RESET_TEXT_COLOR + "\n");
            }
            if (endPosition == null) {
                System.out.print(SET_TEXT_COLOR_RED + "ERROR: Invalid move end position." + RESET_TEXT_COLOR + "\n");
            }

            return;
        }

        ChessPiece.PieceType promotionPiece = null;

        if(commandList.size() == 4) {
            String promotion = commandList.get(3).toUpperCase();

            if (Objects.equals(promotion, "QUEEN")) {
                promotionPiece = ChessPiece.PieceType.QUEEN;
            } else if (Objects.equals(promotion, "ROOK")) {
                promotionPiece = ChessPiece.PieceType.ROOK;
            }  else if (Objects.equals(promotion, "KNIGHT")) {
                promotionPiece = ChessPiece.PieceType.KNIGHT;
            }  else if (Objects.equals(promotion, "BISHOP")) {
                promotionPiece = ChessPiece.PieceType.BISHOP;
            } else {
                System.out.print(SET_TEXT_COLOR_RED + "ERROR: Invalid promotion type." + RESET_TEXT_COLOR + "\n");
                return;
            }
        }

        ChessMove move = new ChessMove(startPosition, endPosition, promotionPiece);
        try {
            chessGame.makeMove(move);
            chessBoard = chessGame.getBoard();
        } catch (InvalidMoveException e) {
            System.out.print(SET_TEXT_COLOR_RED + "ERROR: Invalid chess move." + RESET_TEXT_COLOR + "\n");
            System.out.print(SET_TEXT_COLOR_BLUE + "HINT: Run the 'highlight position' command for valid moves." + RESET_TEXT_COLOR + "\n");
            System.out.print(SET_TEXT_COLOR_BLUE + "EXAMPLE: highlight a1 - gives a list of valid moves for the piece on a1." + RESET_TEXT_COLOR + "\n");

            return;
        }

        System.out.print(SET_TEXT_COLOR_GREEN + "Move completed successfully." + RESET_TEXT_COLOR + "\n");

        printBoard(chessBoard, color, null);
    }

    private void redraw(ArrayList<String> commandList) {
        if (commandList.size() != 1 ||
                !Objects.equals(status, "LOGGED_IN") || Objects.equals(gameStatus, "OUT_OF_GAME")) {
            invalidCommand();
            return;
        }

        printBoard(chessBoard, color, null);
    }

    private void leave(ArrayList<String> commandList) {
        if (commandList.size() != 1 ||
                !Objects.equals(status, "LOGGED_IN") || !Objects.equals(gameStatus, "OBSERVING")) {
            invalidCommand();
            return;
        }

        gameStatus = "OUT_OF_GAME";
        System.out.print(SET_TEXT_COLOR_GREEN + "You have left successfully." + RESET_TEXT_COLOR + "\n");
    }

    private void resign(ArrayList<String> commandList) {
        if (commandList.size() != 1 ||
                !Objects.equals(status, "LOGGED_IN") || !Objects.equals(gameStatus, "PLAYING")) {
            invalidCommand();
            return;
        }

        gameStatus = "OUT_OF_GAME";
        System.out.print(SET_TEXT_COLOR_GREEN + "You have resigned successfully." + RESET_TEXT_COLOR + "\n");
    }

    private void highlight(ArrayList<String> commandList) {
        if (commandList.size() != 2 ||
                !Objects.equals(status, "LOGGED_IN") || !Objects.equals(gameStatus, "PLAYING")) {
            invalidCommand();
            return;
        }
        ChessPosition startPosition = validatePosition(commandList.get(1));

        if (!Objects.equals(chessBoard.getPiece(startPosition).getTeamColor(), color)) {
            System.out.print(SET_TEXT_COLOR_RED + "ERROR: Cannot highlight pieces of the opposite color." + RESET_TEXT_COLOR + "\n");
            return;
        }

        Collection<ChessMove> validMoves = chessGame.validMoves(startPosition);

        Collection<ChessPosition> validPositions = new ArrayList<ChessPosition>();
        validMoves.forEach(x -> validPositions.add(x.getEndPosition()));

        printBoard(chessBoard, color, validPositions);
    }

    private void parseCommand(String line) {
        var args = line.split(" ");
        var serializer = new Gson();

        ArrayList<String> commandList = new ArrayList<>(Arrays.asList(args));

        String command = commandList.getFirst().toLowerCase();

        if (Objects.equals(command, "register")) {
            register(commandList);
        } else if (Objects.equals(command, "login")) {
            login(commandList);
        } else if (Objects.equals(command, "quit")) {
            quit(commandList);
        } else if (Objects.equals(command, "help")) {
            if (!Objects.equals(commandList.size(), 1)) {
                invalidCommand();
                return;
            }

            help();
        } else if (Objects.equals(command, "create")) {
            create(commandList);
        } else if (Objects.equals(command, "list")) {
            list(commandList);
        } else if (Objects.equals(command, "join")) {
            join(commandList);
        } else if (Objects.equals(command, "observe")) {
            observe(commandList);
        } else if (Objects.equals(command, "logout")) {
            logout(commandList);
        } else if (Objects.equals(command, "clear")) {
            clearDatabase();
        } else if (Objects.equals(command, "redraw")) {
            redraw(commandList);
        } else if (Objects.equals(command, "leave")) {
            leave(commandList);
        } else if (Objects.equals(command, "move")) {
            move(commandList);
        } else if (Objects.equals(command, "resign")) {
            resign(commandList);
        } else if (Objects.equals(command, "highlight")) {
            highlight(commandList);
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
            if (Objects.equals(gameStatus, "OUT_OF_GAME")) {
                System.out.print("[" + status + "] >>> ");
            } else {
                System.out.print("[" + gameStatus + "] >>> ");
            }
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            parseCommand(line);
            System.out.println();
        }
    }
}
