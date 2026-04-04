package client;

import chess.*;
import com.google.gson.Gson;
import helpers.InGame;
import helpers.Output;
import model.AuthData;
import requests.Response;

import java.lang.reflect.Field;
import java.util.*;

import static helpers.Output.*;
import static helpers.Validation.validatePosition;
import static helpers.PrintBoard.printBoard;
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
    private static ChessGame.TeamColor color;

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
            color = ChessGame.TeamColor.WHITE;
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
                String colorCommand = commandList.get(2);

                if (Objects.equals(colorCommand, "BLACK")) {
                    color = ChessGame.TeamColor.BLACK;
                } else {
                    color = ChessGame.TeamColor.WHITE;
                }

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
            System.out.print(SET_TEXT_COLOR_BLUE + "EXAMPLE: highlight a1 - gives a list of valid moves for the piece on a1."
                    + RESET_TEXT_COLOR + "\n");

            return;
        }

        System.out.print(SET_TEXT_COLOR_GREEN + "Move completed successfully." + RESET_TEXT_COLOR + "\n");

        printBoard(chessBoard, color, null);
    }

    private void parseCommand(String line) {
        var args = line.split(" ");

        ArrayList<String> commandList = new ArrayList<>(Arrays.asList(args));
        String command = commandList.getFirst().toLowerCase();

        InGame gameActions = new InGame(status, gameStatus, chessBoard,
                chessGame, color);

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

            Output out = new Output(status, gameStatus);
            out.help();
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
            gameActions.redraw(commandList);
        } else if (Objects.equals(command, "leave")) {
            gameActions.leave(commandList);
        } else if (Objects.equals(command, "move")) {
            move(commandList);
        } else if (Objects.equals(command, "resign")) {
            gameActions.resign(commandList);
        } else if (Objects.equals(command, "highlight")) {
            gameActions.highlight(commandList);
        } else {
            invalidCommand();
        }
    }

    void main(String[] args) {
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
