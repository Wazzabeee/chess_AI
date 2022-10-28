import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.List;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Search for best moves
 */
public class Search {

    public static class Node{
        protected String move;
        protected double score;

        public Node(String move, double score){
            this.move = move;
            this.score = score;
        }
    }

    public static int nodesExplored = 0;
    public Node minimax(Board board, Integer depth, Double alpha, Double beta, Boolean playerToMaximize, Integer numberOfMoves)
    {
        if (depth == 0 || board.isDraw() || board.isMated() || board.isStaleMate()) // if trivial case
        {
            return new Node("None", Evaluator.scoresFromFen(board, board.getFen(), numberOfMoves, playerToMaximize));
        }

        List<Move> children = board.legalMoves(); // generate all children from current board state
        int randomNum = new Random().nextInt(children.size()); // generate a random number in [0; children.size-1]
        Move best_move = children.get(randomNum); // select random move from children

        if (playerToMaximize)
        {
            double maxEval = -Double.MAX_VALUE;
            for (Move move : children)
            {
                incrementNodesCount();
                board.doMove(move);
                // Compare currentEval with maxEval and alpha beta prunnings
                double currentEval = minimax(board, depth - 1, alpha, beta, false, children.size()).score;
                board.undoMove();

                if (currentEval > maxEval)
                {
                    maxEval = currentEval;
                    best_move = move;
                }
                alpha = max(alpha, maxEval);
                if (beta <= alpha)
                {
                    break;
                }
            }
            return new Node(best_move.toString(), maxEval);
        }
        else
        {
            double minEval = Double.MAX_VALUE;
            for (Move move : children)
            {
                incrementNodesCount();
                board.doMove(move);
                double currentEval = minimax(board, depth - 1, alpha, beta, true, children.size()).score;
                board.undoMove();

                if (currentEval < minEval)
                {
                    minEval = currentEval;
                    best_move = move;
                }
                // alpha ?
                beta = min(beta, minEval);
                if (beta <= alpha)
                {
                    break;
                }
            }
            return new Node(best_move.toString(), minEval);
        }
    }

    private static void incrementNodesCount() {
        nodesExplored++;
    }
    public int getNodesExplored() {
        return nodesExplored;
    }
    public void resetNodesExplored()
    {
        nodesExplored = 0;
    }
}
