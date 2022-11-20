import com.github.bhlangonijr.chesslib.move.Move;

public class Result {
    
    private final Double num;
    private final Move bestMove;

    private final Integer nodeExplored;

    public Result(Double num, Move bestMove, Integer nodeExplored) {
        this.num = num;
        this.bestMove = bestMove;
        this.nodeExplored = nodeExplored;
    }

    public Double getNum() {
        return num;
    }

    public Move getBestMove() {
        return bestMove;
    }

    public Integer getNodeExplored() {
        return nodeExplored;
    }
    
}
