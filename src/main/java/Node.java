/* Basic Node Class. */

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.move.Move;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Node implements Callable<Result> {

    // private String name;
    
    private final Board board;
    private final Integer depth;
    private final Boolean playerToMaximize;
    private final double alpha;
    private final double beta;

    private final Move move;
    private Integer nodesExplored;

    public Node(Board board, Integer depth, Boolean playerToMaximise, Move move, LeftSideNode parent) {
        this.board = board.clone();
        this.depth = depth;
        this.playerToMaximize = playerToMaximise;
        this.nodesExplored = 0;
        this.move = move;

        this.alpha = parent.getAlpha();
        this.beta = parent.getBeta();

        // this.name = this.move.toString();
    }

    public Result alphaBetaCutOff(Board board, Integer depth, Double alpha, Double beta, Boolean playerToMaximize) {
        // Cas Trivial
        if (depth == 0 || board.isDraw() || board.isMated() || board.isStaleMate()) {
            return new Result(Evaluator.scoresFromFen(board), null, 0);
        } 

        // Génère la liste des mouvements possibles
        List<Move> children = board.legalMoves();

        // Trie du mouvement le plus intéressant au moins intéressant
        children.sort(Comparator.comparingInt((Move m) -> (int) getMoveScore(board, m)));
        Collections.reverse(children);

        Move bestMove = children.get(0);

        if (playerToMaximize) {
            Double maxEval = alpha;

            for (Move move : children) {
                incrementNodesCount();
                board.doMove(move);
                double currentEval = alphaBetaCutOff(board, depth - 1, alpha, beta, false).getNum();
                board.undoMove();

                if (maxEval < currentEval) {
                    maxEval = currentEval;
                    bestMove = move;
                }

                alpha = max(alpha, maxEval);

                if (beta <= alpha) {
                    break;
                }
            }

            return new Result(maxEval, bestMove, this.nodesExplored);
        } else {
            Double minEval = beta;

            for (Move move : children) {
                incrementNodesCount();
                board.doMove(move);
                double currentEval = alphaBetaCutOff(board, depth - 1, alpha, beta, true).getNum();
                board.undoMove();

                if (currentEval < minEval) {
                    minEval = currentEval;
                    bestMove = move;
                }

                beta = min(beta, minEval);

                if (beta <= alpha) {
                    break;
                }
            }

            return new Result(minEval, bestMove, this.nodesExplored);
        }
    }

    private static long getMoveScore(Board b, Move move)
    {
        Piece attackedPiece = b.getPiece(move.getTo());
        Piece attackingPiece = b.getPiece(move.getFrom());

        if (attackedPiece != Piece.NONE)
            return Evaluator.getPieceStaticValue(attackedPiece);
        if (move.getPromotion() != Piece.NONE)
            return Evaluator.getPieceStaticValue(move.getPromotion());

        return Evaluator.getSquareStaticValue(attackingPiece, move.getTo());

    }

    private void incrementNodesCount() {
        this.nodesExplored++;
    }

    public Integer getNodesExplored() {
        return this.nodesExplored;
    }

    @Override
    public Result call() {
        Result r = this.alphaBetaCutOff(this.board, this.depth, this.alpha, this.beta, this.playerToMaximize);

        //System.out.println("Node : " + this.name + " | score : " + r.getNum());

        return new Result(r.getNum(), this.move, this.nodesExplored);
    }
}
