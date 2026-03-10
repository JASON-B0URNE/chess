package client;

import chess.*;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ClientMain {
    private static String status = "LOGGED_OUT";
    private static boolean quit = false;

    private static void parseCommand(String line) {
        var args = line.split(" ");

        ArrayList<String> commandList = new ArrayList<>();
        for (var arg : args) {
            commandList.add(arg);
        }

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

        } else {
            //TODO: ERROR OUT
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
