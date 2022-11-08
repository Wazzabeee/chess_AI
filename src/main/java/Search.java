/* Search Class. */

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.Piece;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Search {

    public static class Answer{
        protected String move;
        protected double score;

        public Answer(String move, double score){
            this.move = move;
            this.score = score;
        }
    }

    public static int nodesExplored = 0;
    public Answer minimax(Board board, Integer depth, Double alpha, Double beta, Boolean playerToMaximize, Integer numberOfMoves) {
        // Cas Terminal
        if (depth == 0 || board.isDraw() || board.isMated() || board.isStaleMate()) {
            return new Answer(null, Evaluator.scoresFromFen(board, numberOfMoves, playerToMaximize));
        }

        // Génère la liste des mouvements possibles
        List<Move> children = board.legalMoves();

        // Trie du mouvement le plus intéressant au moins intéressant
        children.sort(Comparator.comparingInt((Move m) -> (int) getMoveScore(board, m)));
        Collections.reverse(children);

        // Selectionne le meilleur mouvement
        Move best_move = children.get(0);

        if (playerToMaximize) {
            double maxEval = -Double.MAX_VALUE;

            for (Move move : children) {
                incrementNodesCount();
                board.doMove(move);
                double currentEval = minimax(board, depth - 1, alpha, beta, false, children.size()).score;
                board.undoMove();

                // Compare currentEval with maxEval and alpha beta prunnings
                if (currentEval > maxEval) {
                    maxEval = currentEval;
                    best_move = move;
                }

                alpha = max(alpha, maxEval);

                if (beta <= alpha) {
                    break;
                }
            }

            return new Answer(best_move.toString(), maxEval);
        } else {
            double minEval = Double.MAX_VALUE;
            for (Move move : children) {
                incrementNodesCount();
                board.doMove(move);
                double currentEval = minimax(board, depth - 1, alpha, beta, true, children.size()).score;
                board.undoMove();

                if (currentEval < minEval) {
                    minEval = currentEval;
                    best_move = move;
                }

                beta = min(beta, minEval);

                if (beta <= alpha) {
                    break;
                }
            }

            return new Answer(best_move.toString(), minEval);
        }
    }

    public static long getMoveScore(Board b, Move move)
    {
        Piece attackedPiece = b.getPiece(move.getTo());
        Piece attackingPiece = b.getPiece(move.getFrom());

        if (attackedPiece != Piece.NONE)
            return Evaluator.getPieceStaticValue(attackedPiece);
        if (move.getPromotion() != Piece.NONE)
            return Evaluator.getPieceStaticValue(move.getPromotion());

        return Evaluator.getSquareStaticValue(attackingPiece, move.getTo());

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
