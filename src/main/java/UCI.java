import java.util.*;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import org.jetbrains.annotations.NotNull;

public class UCI {
    static String ENGINENAME="IA_v0.1";
    public static void uciCommunication() {
        Scanner input = new Scanner(System.in);
        Board board = new Board();
        while (true)
        {
            String inputString=input.nextLine();
            if ("uci".equals(inputString))
            {
                inputUCI();
            }
            else if (inputString.startsWith("setoption"))
            {
                inputSetOption(inputString);
            }
            else if ("isready".equals(inputString))
            {
                inputIsReady();
            }
            else if ("ucinewgame".equals(inputString))
            {
                inputUCINewGame();
            }
            else if (inputString.startsWith("position"))
            {
                //System.out.println("postion recognized");
                inputPosition(inputString, board);
            }
            //else if ("go".equals(inputString))
            else if (inputString.startsWith("go"))
            {
                inputGo(board);
            }
        }
    }
    public static void inputUCI() {
        System.out.println("id name "+ENGINENAME);
        System.out.println("id author Clément & Justin");
        //options go here
        System.out.println("uciok");
    }
    public static void inputSetOption(String inputString) {
        //set options
    }
    public static void inputIsReady() {
        System.out.println("readyok");
    }
    public static void inputUCINewGame() {
        //add code here
    }
    public static void inputPosition(String input, Board board) {
        //System.out.println(input);
        input=input.substring(9).concat(" ");
        if (input.contains("startpos ")) {
            input=input.substring(9);
        }
        else if (input.contains("fen")) {
            input=input.substring(4);
            board.loadFromFen(input);
        }
        if (input.contains("moves")) {
            input=input.substring(input.indexOf("moves")+6);
            String[] moves = input.split("\\s+");

            if (moves.length > 1) //if not first move
            {
                board.doMove(moves[moves.length - 2]);
            }
            board.doMove(moves[moves.length - 1]);
        }
        }
    public static void inputGo(@NotNull Board board) {
        //search for first move
        List<Move> moves = board.legalMoves();
        System.out.println("Legal moves: " + moves);
        System.out.println("bestmove " + moves.get(0));
    }
}