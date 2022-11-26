import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class TranspositionTable {
    private final Map<Long,hashNode> transpositionTable = new HashMap<>(64000);

    public static class hashNode{
        protected double value;
        protected int depth;
        protected int type; // 0 = noeud exact / 1 = fourchette haute / -1 = fourchette basse
        public hashNode(double value, int depth, int type){
            this.value = value;
            this.depth = depth;
            this.type = type;
        }
    }

    public double probeHash(long hashKey, int depth, double alpha, double beta)
    {
        if (transpositionTable.containsKey(hashKey))
        {
            hashNode current = new hashNode(transpositionTable.get(hashKey).value,
                    transpositionTable.get(hashKey).depth,
                    transpositionTable.get(hashKey).type);

            if (current.depth > depth)
            {
                if (current.type == 0)
                {
                    return current.value;
                }
                if (current.type == 1 && current.value <= alpha)
                {
                    return alpha;
                }
                if (current.type == -1 && current.value >= beta)
                {
                    return beta;
                }
            }
        }
        return 99999999999.0;
    }

    public void recordHash(long hashKey, int depth, double val, int type)
    {
        transpositionTable.put(hashKey, new hashNode(val, depth, type));
    }

    public Result alphaBetaTranspositionTable(Board board, Integer depth, Double alpha, Double beta, Boolean playerToMaximize) {

        double hashvalue = probeHash(board.getIncrementalHashKey(), depth, alpha, beta); // recherche dans la table
        if (hashvalue != 99999999999.0) // si hashValue cohérent
        {
            return new Result(hashvalue, null, 0);
        }

        // Cas Trivial
        if (depth == 0 || board.isDraw() || board.isMated() || board.isStaleMate()) {
            return new Result(BasicEvaluation.evaluate(board), null, 0);
        }

        // Génère la liste des mouvements possibles
        List<Move> children = board.legalMoves();

        // Trie du mouvement le plus intéressant au moins intéressant
        children.sort(Comparator.comparingInt((Move m) -> (int) Node.getMoveScore(board, m)));
        Collections.reverse(children);

        Move bestMove = children.get(0);

        if (playerToMaximize) {
            int type = 1; // type de noeud pour TT
            double maxEval = alpha;

            for (Move move : children) {
                board.doMove(move);
                double currentEval = alphaBetaTranspositionTable(board, depth - 1, alpha, beta, false).num();
                board.undoMove();

                if (maxEval < currentEval) {
                    type = 0; // mise à jour du type de noeud TT
                    maxEval = currentEval;
                    bestMove = move;
                }

                alpha = max(alpha, maxEval);

                if (beta <= alpha) {
                    break;
                }
            }
            recordHash(board.getIncrementalHashKey(), depth, alpha, type); // ajout du noeud dans la TT
            return new Result(maxEval, bestMove, 0);
        } else {
            int type = -1;
            double minEval = beta;

            for (Move move : children) {
                board.doMove(move);
                double currentEval = alphaBetaTranspositionTable(board, depth - 1, alpha, beta, true).num();
                board.undoMove();

                if (currentEval < minEval) {
                    type = 0; // MAJ du type de noeud TT
                    minEval = currentEval;
                    bestMove = move;
                }

                beta = min(beta, minEval);

                if (beta <= alpha) {
                    break;
                }
            }
            recordHash(board.getIncrementalHashKey(), depth, beta, type); // ajout du noeud dans la TT
            return new Result(minEval, bestMove, 0);
        }
    }
}
