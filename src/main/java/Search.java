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

    public static String[] minimax(Board board, Integer depth, Integer alpha, Integer beta, Boolean playerToMaximize)
    {
        List<Move> children = board.legalMoves(); // generate all children from current board state

        if (depth == 0 || board.isDraw() || board.isMated() || board.isStaleMate()) // if trivial case
        {
            return new String[] {"None", Integer.toString(Evaluator.scoresFromFen(board.getFen()))};
        }

        int randomNum = new Random().nextInt(children.size()); // generate a random number in [0; children.size-1]
        Move best_move = children.get(randomNum); // select random move from children

        if (playerToMaximize)
        {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : children)
            {
                // Copy current board state and play current move
                Board temp_board = new Board();
                temp_board.loadFromFen(board.getFen());
                temp_board.doMove(move);

                // Compare currentEval with maxEval and alpha beta prunnings
                int currentEval = Integer.parseInt(minimax(temp_board, depth - 1, alpha, beta, false)[1]);
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
            return new String[] {best_move.toString(), Integer.toString(maxEval)};
        }
        else
        {
            int minEval = Integer.MAX_VALUE;
            for (Move move : children)
            {
                Board temp_board = new Board();
                temp_board.loadFromFen(board.getFen());
                temp_board.doMove(move);

                int currentEval = Integer.parseInt(minimax(temp_board, depth - 1, alpha, beta, true)[1]);
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
            return new String[] {best_move.toString(), Integer.toString(minEval)};
        }
    }
}
