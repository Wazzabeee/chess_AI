
public class Main {
    public static void main(String[] args) {
        //UCI.uciCommunication();
        int[] scores = Evaluator.scoresFromFen("rnbqkbnr/1ppppppp/8/8/4P3/1p3N2/P1PP1PPP/RNBQKB1R w KQkq - 0 4");
        System.out.println(scores[0]);
        System.out.println(scores[1]);

        //uci
        //isready
        //position startpos moves e2e4
        //go wtime 30000 btime 30000 winc 0 binc 0
        //position startpos moves e2e4 a7a5 g1f3
        //go
    }
}