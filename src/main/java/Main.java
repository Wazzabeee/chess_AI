/* Entry Point. */

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;

import java.time.Duration;
import java.time.Instant;

public class Main {
    public static void main(String[] args) {

        UCI.uciCommunication();
        /*
        Board b = new Board();
        b.loadFromFen("Q3kb1r/2pq1ppp/3p4/pp2p1B1/7N/1B6/1PP1N3/R3K3 b k - 0 24");
        Stop stop = new Stop();
        LeftSideNode root = new LeftSideNode(b, 5, -Double.MAX_VALUE, Double.MAX_VALUE, b.getSideToMove() == Side.WHITE, stop);
        Timer timer = new Timer(root, stop);
        timer.start();
        Instant start = Instant.now();
        System.out.println("LeftSideNode depth : " + 5 + " start");
        Result r = root.PVS();
        System.out.println("LeftSideNode depth : " + 5 + " stop");
        Instant finish = Instant.now();

        if (!stop.getStop()) {
            System.out.println("Timer stop");
            stop.setTrueStop();
            timer.interrupt();
        }

        System.out.println("bestmove " + r.getBestMove());
        System.out.println("LeftSideNode found in " + Duration.between(start, finish).toMillis() + "ms | " + r.getNodeExplored() + " nodes explored | score : " + r.getNum() + " | depth = " + 5 );*/
    }
}