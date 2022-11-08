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
    
    private Board board;
    private Integer depth;
    private Boolean playerToMaximize;
    private LeftSideNode parent;

    private Move bestMove;
    private Integer nodesExplored;

    public Node(Board board, Integer depth, Boolean playerToMaximise, Move bestMove, LeftSideNode parent) {
        this.board = board.clone();
        this.depth = depth;
        this.playerToMaximize = playerToMaximise;
        this.nodesExplored = 0;
        this.bestMove = bestMove;
        this.parent = parent;
    }

    public Double alphaBetaCutOff(Board board, Integer depth, Boolean playerToMaximize, Integer numberOfMoves) {
        // Cas Trivial
        if (depth == 0 || board.isDraw() || board.isMated() || board.isStaleMate()) {
            return Evaluator.scoresFromFen(board, numberOfMoves, playerToMaximize);
        } 

        // Génère la liste des mouvements possibles
        List<Move> children = board.legalMoves();

        // Trie du mouvement le plus intéressant au moins intéressant
        children.sort(Comparator.comparingInt((Move m) -> (int) getMoveScore(board, m)));
        Collections.reverse(children);

        if (playerToMaximize) {
            Double maxEval = null;
            for (Move move : children) {
                incrementNodesCount();
                board.doMove(move);
                double currentEval = alphaBetaCutOff(board, depth - 1, !playerToMaximize, children.size());
                board.undoMove();

                // Compare currentEval with maxEval and alpha beta prunnings
                maxEval = this.parent.getScore();
                if (maxEval < currentEval) {
                    this.parent.setScore(currentEval, this.bestMove);
                }

                /*if (this.parent.getAlpha() < maxEval) {
                    this.parent.setAlpha(maxEval);
                    break;
                }*/
                

                this.parent.setAlpha(max(this.parent.getAlpha(), maxEval));

                if (this.parent.getBeta() <= this.parent.getAlpha()) {
                    break;
                }
            }

            return maxEval;
        } else {
            Double minEval = null;
            for (Move move : children) {
                incrementNodesCount();
                board.doMove(move);
                double currentEval = alphaBetaCutOff(board, depth - 1, !playerToMaximize, children.size());
                board.undoMove();

                minEval = this.parent.getScore();
                if (currentEval < minEval) {
                    this.parent.setScore(currentEval, this.bestMove);
                };

                /*if (minEval < this.parent.getBeta()) {
                    this.parent.setBeta(minEval);
                    break;
                }*/

                this.parent.setBeta(min(this.parent.getBeta(), minEval));

                if (this.parent.getBeta() <= this.parent.getAlpha()) {
                    break;
                }
            }

            return minEval;
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

    /*public String getBestMove() {
        String ans = this.bestMove.toString();
        if (ans == null) {
            return " ";
        }
        return this.bestMove.toString();
    }*/

    private void incrementNodesCount() {
        this.nodesExplored++;
    }

    public Integer getNodesExplored() {
        return this.nodesExplored;
    }

    @Override
    public Result call() {
        Double num = this.alphaBetaCutOff(board.clone(), this.depth, this.playerToMaximize, 20);

        return new Result(num, this.bestMove, this.nodesExplored);
    }
}
