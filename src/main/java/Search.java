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

    public static String[] minimax(Board board, Integer depth, Double alpha, Double beta, Boolean playerToMaximize, Integer numberOfMoves)
    {
        if (depth == 0 || board.isDraw() || board.isMated() || board.isStaleMate()) // if trivial case
        {
            return new String[] {"None", String.valueOf(Evaluator.scoresFromFen(board, board.getFen(), numberOfMoves, playerToMaximize))};
        }

        List<Move> children = board.legalMoves(); // generate all children from current board state
        int randomNum = new Random().nextInt(children.size()); // generate a random number in [0; children.size-1]
        Move best_move = children.get(randomNum); // select random move from children

        if (playerToMaximize)
        {
            double maxEval = -Double.MIN_VALUE;
            for (Move move : children)
            {
                board.doMove(move);
                // Compare currentEval with maxEval and alpha beta prunnings
                double currentEval = Double.parseDouble(minimax(board, depth - 1, alpha, beta, false, children.size())[1]);
                board.undoMove();

                if (currentEval > maxEval)
                {
                    maxEval = currentEval;
                    best_move = move;
                }
                alpha = max(alpha, currentEval);
                if (beta <= alpha)
                {
                    break;
                }
            }
            return new String[] {best_move.toString(), String.valueOf(maxEval)};
        }
        else
        {
            double minEval = Double.MAX_VALUE;
            for (Move move : children)
            {
                board.doMove(move);
                double currentEval = Double.parseDouble(minimax(board, depth - 1, alpha, beta, true, children.size())[1]);
                board.undoMove();

                if (currentEval < minEval)
                {
                    minEval = currentEval;
                    best_move = move;
                }
                alpha = min(alpha, currentEval);
                if (beta <= alpha)
                {
                    break;
                }
            }
            return new String[] {best_move.toString(), String.valueOf(minEval)};
        }
    }
}
