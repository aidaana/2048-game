import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsoleVersion {
    public static void main(String[] args) {
        Board board = new Board();
        AtomicBoolean lost = new AtomicBoolean(false);

        board.addGameOverListener((()-> {
            System.out.println("You lost.");
            lost.set(true);
        }));

        board.addWinListener((()-> {
            System.out.println("You won! You can continue to play or type \"restart\" to start a new game.");
        }));

        System.out.println("\nJoin the numbers and get to the 2048 tile! \nUse \"up\", \"down\", \"left\", \"right\" commands to move the board." +
                "\nUse \"restart\" to start a new game.\n");
        System.out.println(board);

        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            String command = sc.nextLine().trim().toLowerCase();
            switch (command) {
                case "up": board.moveUp(); break;
                case "down": board.moveDown(); break;
                case "left": board.moveLeft(); break;
                case "right": board.moveRight(); break;
                case "restart": board = new Board(); break;
                case "present": break;
                default:
                    System.out.println("Please provide a valid command. Use \"up\", \"down\", \"left\", \"right\" commands to move the board.");
            }
            System.out.println(board);
            if(lost.get()) {
                board = new Board();
            }
        }
    }
}
