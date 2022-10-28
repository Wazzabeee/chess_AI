import java.time.Duration;
import java.time.Instant;
import java.util.*;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;

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
                inputUCINewGame(board);
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
    public static void inputUCINewGame(Board board) {
        board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
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
    public static void inputGo(Board board) {
        //search for move
        // Player to maximize :
        // If AI plays white : true else black
        Search s = new Search();
        Instant start = Instant.now();
        String[] answer = s.minimax(board, 4, -Double.MAX_VALUE, Double.MAX_VALUE, board.getSideToMove() == Side.WHITE, 24);
        Instant finish = Instant.now();
        System.out.println(Duration.between(start, finish).toMillis());   //in millis
        System.out.println("bestmove " + answer[0]);
        System.out.println(s.getNodesExplored());

    }
}