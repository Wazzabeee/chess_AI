/* UCI Protocol Class. */

import java.time.Duration;
import java.time.Instant;

import java.util.*;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;

public class UCI {
    static String ENGINENAME="IA_v0.1";
    static Boolean continueOpening = true;
    public static void uciCommunication() {
        Scanner input = new Scanner(System.in);

        Board board = new Board();

        while (true) {
            String inputString=input.nextLine();
            if ("uci".equals(inputString)) {
                inputUCI();
            } else if (inputString.startsWith("setoption")) {
                inputSetOption(inputString);
            } else if ("isready".equals(inputString)) {
                inputIsReady();
            } else if ("ucinewgame".equals(inputString)) {
                inputUCINewGame(board);
            } else if (inputString.startsWith("position")) {
                //System.out.println("postion recognized");
                inputPosition(inputString, board);
            } else if (inputString.startsWith("go")) {
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
        } else if (input.contains("fen")) {
            input=input.substring(4);
            board.loadFromFen(input);
        }

        if (input.contains("moves")) {
            input=input.substring(input.indexOf("moves")+6);
            MoveList list = new MoveList();
            list.loadFromSan(input);
            board.loadFromFen(list.getFen());
            /*input=input.substring(input.indexOf("moves")+6);
            String[] moves = input.split("\\s+");

            if (moves.length > 1) {
                board.doMove(moves[moves.length - 2]);
            }

            board.doMove(moves[moves.length - 1]);*/
        }
    } 

    public static void search(Board board, int depth) {
        Stop stop = new Stop();

        LeftSideNode root = new LeftSideNode(board, depth, -Double.MAX_VALUE, Double.MAX_VALUE, board.getSideToMove() == Side.WHITE, stop);
        Timer timer = new Timer(root, stop);
        timer.start();
        Instant start = Instant.now();
        Result r = root.PVS();
        Instant finish = Instant.now();

        if (!stop.getStop()) {
            stop.setTrueStop();
            timer.interrupt();
        }

        System.out.println("bestmove " + r.getBestMove());
        System.out.println("LeftSideNode found in " + Duration.between(start, finish).toMillis() + "ms | " + r.getNodeExplored() + " nodes explored | score : " + r.getNum() + " | depth = " + depth );
    }
    public static void inputGo(Board board) {
        //search for move
        // Player to maximize :
        // If AI plays white : true else black
        int depth = 5;
        if(continueOpening) {
            Instant start = Instant.now();
            readBook rb = new readBook();
            Move move = rb.getMove(board);

            if (move == null) {
                continueOpening = false;
                System.out.println("fin de l'opening");
                search(board, depth);
            }
            else {
                Instant finish = Instant.now();
                System.out.println("bestmove " + move);
                System.out.println("found in " + Duration.between(start, finish).toMillis() + "ms");
            }

        }
        else
            search(board, depth);
    }
}