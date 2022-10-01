import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Scanner;

public class SudokuSolver {
    private static final int BOARD_SIZE = 9;

    public static void main(String[] args) {
        ArrayList<Integer[]> board = null;

        if (getSudokuBoard() != null) {
            board = getSudokuBoard();
        }

        printBoard(board);

        if (solveBoard(board)) {
            System.out.println("Solve successful!");
        } else {
            System.out.println("Invalid board!");
        }

        printBoard(board);
    }

    // prints the board arraylist and arrays
    private static void printBoard(ArrayList<Integer[]> board) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            if (row % 3 == 0 && row != 0) {
                System.out.println("-----------");
            }
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (col % 3 == 0 && col != 0) {
                    System.out.print("|");
                }
                System.out.print(board.get(row)[col]);
            }
            System.out.println();
        }
    }

    // check if a given number is in a row
    private static boolean isNumInRow(ArrayList<Integer[]> board, int number, int row) {

        for (int i = 0; i < BOARD_SIZE; i++) {
            if (board.get(row)[i] == number) {
                return true;
            }
        }
        return false;
    }

    // check if a given number is in a col
    private static boolean isNumInColumn(ArrayList<Integer[]> board, int number, int col) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (board.get(i)[col] == number) {
                return true;
            }
        }
        return false;
    }

    // check if a given number is in a box
    private static boolean isNumInBox(ArrayList<Integer[]> board, int number, int row, int col) {
        int localBoxRow = row - row %  3;
        int localBoxColumn = col - col % 3;

        for (int i = localBoxRow; i < localBoxColumn; i++) {
            for (int j = localBoxColumn; j < localBoxColumn; j++) {
                if (board.get(i)[j] == number) {
                    return true;
                }
            }
        }
        return false;
    }

    // checks if the number placement is valid by checking the row, col, and box
    private static boolean isValidPlacement(ArrayList<Integer[]> board, int number, int row, int col) {
        return !isNumInRow(board, number, row) &&
                !isNumInColumn(board, number, col) &&
                !isNumInBox(board, number, row, col);
    }

    // recursively solve the board by checking for valid placements
    public static boolean solveBoard(ArrayList<Integer[]> board) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board.get(row)[col] == 0) {
                    for (int numberToTry = 1; numberToTry <= BOARD_SIZE; numberToTry++) {
                        if (isValidPlacement(board, numberToTry, row, col)) {
                            board.get(row)[col] = numberToTry;

                            if (solveBoard(board)) {
                                return true;
                            } else {
                                board.get(row)[col] = 0;
                            }
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    // API call for randomly generated board
    public static ArrayList<Integer[]> getSudokuBoard() {
        Integer[] newRow;
        String apiKeyStr;

        // read API key from file
        try {
            File apiKey = new File("api.txt");
            Scanner scan = new Scanner(apiKey);
            apiKeyStr = scan.nextLine();
            scan.close();
        } catch (FileNotFoundException ex) {//FileNotFoundException
            System.out.println("No api.key found.");
            return null;
        }

        // initialize board
        ArrayList<Integer[]> board = new ArrayList<>();

        String bo = "";

        // attempt an HTTP request for board
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://sudoku-board.p.rapidapi.com/new-board?diff=2&stype=list&solu=true"))
                    .header("X-RapidAPI-Key", apiKeyStr)
                    .header("X-RapidAPI-Host", "sudoku-board.p.rapidapi.com")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jo = new JSONObject(response.body());

            // convert JSON to pure string of ints
            bo = jo.getJSONObject("response").get("unsolved-sudoku").toString().replace("[", "").replace("]", "").replace(",", "");
        } catch (Exception ex) {
            System.out.println("Unable to connect. Invalid API key or no connection.");
            return null;
        }

        // count of every 9 ints
        int count = 0;

        // new int array for int arraylist
        newRow = new Integer[9];

        // fill the array list with arrays
        for (int i = 0; i < bo.length(); i++) {
            count++;
            int a = Integer.parseInt(String.valueOf(bo.charAt(i)));
            newRow[count - 1] = a;

            // start over at 9 ints
            if ((count % 9) == 0) {
                count = 0;
                board.add(newRow);
                newRow = new Integer[9];
            }
        }
        return board;
    }
}
