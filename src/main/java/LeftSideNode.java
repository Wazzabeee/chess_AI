/* Basic Node Class. */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.move.Move;

import static java.lang.Math.min;

public class LeftSideNode {
    
    private Board board;
    private Integer depth;
    private Double alpha;
    private Double beta;
    private Boolean playerToMaximize;
    private Integer nodesExplored;
    private Double score;

    private Move bestMove;
    private Stop stop;

    private List<Move> children;

    private ExecutorService executor;

    private Boolean isActive;
    private LeftSideNode fils;

    public LeftSideNode(Board board, Integer depth, Double alpha, Double beta, Boolean playerToMaximise, Stop stop) {
        this.stop = stop;
        this.board = board;
        this.depth = depth;
        this.playerToMaximize = playerToMaximise;
        this.nodesExplored = 0;
        this.alpha = alpha;
        this.beta = beta;
        
        // Récupère les Moves et les tri
        this.children = this.board.legalMoves();
        this.children.sort(Comparator.comparingInt((Move m) -> (int) getMoveScore(this.board, m)));
        Collections.reverse(this.children);

        // Meilleur Move
        try {
            this.bestMove = this.children.get(0);
        }
        catch (Exception e) {}
        this.isActive = false;
        this.fils = null;
    }

    public Result PVS() {
        // Node la plus à Gauche
        if (this.depth == 3 || this.board.isDraw() || this.board.isMated() || this.board.isStaleMate()){
            Node n = new Node(this.board, this.depth, this.playerToMaximize, this.bestMove, this);
            Result r = n.alphaBetaCutOff(this.board, this.depth, this.alpha, this.beta, playerToMaximize);
            return r;
        }

        // On trouve le noeud le plus à gauche
        this.incrementNodesCount(1);
        this.board.doMove(this.bestMove);
        LeftSideNode fils = new LeftSideNode(this.board, this.depth - 1, this.alpha, this.beta, !this.playerToMaximize, this.stop);
        this.fils = fils;

        Result r = fils.PVS();

        this.score = r.getNum();
        this.incrementNodesCount(r.getNodeExplored());;
        this.board.undoMove();

        if (this.stop.getStop() || this.children.size() == 1) {
            return new Result(this.score, this.bestMove, this.nodesExplored);
        }

        // Crée une pool de (15) Thread
        this.executor = Executors.newFixedThreadPool(min(15, this.children.size() - 1));
        List<Future<Result>> resultList = new ArrayList<Future<Result>>();

        this.isActive = true;
        
        // Chacun des noeuds va exécuter Alpha Beta Cut Off
        for (Move m : this.children.subList(1, this.children.size())) {
            this.incrementNodesCount(1);
            board.doMove(m);
            Node n = new Node(this.board, this.depth - 1, !this.playerToMaximize, m, this);
            board.undoMove();

            if (this.isActive()) {
                resultList.add(this.executor.submit(n));
            } else {
                break;
            }
        }

        if (this.isActive()) {
            // Attendre que tous les Nodes donnent un résultat
            this.executor.shutdown();
        } 

        if (!this.isActive()) {
            return new Result(this.score, this.bestMove, this.nodesExplored);
        }

        try { 
            //if (!this.executor.isShutdown()) {
                // this.executor.awaitTermination(200, TimeUnit.MILLISECONDS);
            //}
        
            for (Future<Result> future : resultList) {    
                if (!this.isActive()) {
                    break;
                }

                r = future.get();

                this.incrementNodesCount(r.getNodeExplored());

                if (playerToMaximize) {
                    if (this.score < r.getNum()) {
                        this.score = r.getNum();
                        this.bestMove = r.getBestMove();
                    }
                } else {
                    if (r.getNum() < this.score){
                        this.score = r.getNum();
                        this.bestMove = r.getBestMove();
                    }
                }
            }
        } catch (InterruptedException  e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        this.isActive = false;

        return new Result(this.score, this.bestMove, this.nodesExplored);
    }

    private static long getMoveScore(Board b, Move move) {
        Piece attackedPiece = b.getPiece(move.getTo());
        Piece attackingPiece = b.getPiece(move.getFrom());

        if (attackedPiece != Piece.NONE)
            return Evaluator.getPieceStaticValue(attackedPiece);
        if (move.getPromotion() != Piece.NONE)
            return Evaluator.getPieceStaticValue(move.getPromotion());

        return Evaluator.getSquareStaticValue(attackingPiece, move.getTo());

    }

    public Double getAlpha() {
        return this.alpha;
    }

    public Double getBeta() {
        return this.beta;
    }

    private void incrementNodesCount(int nb) {
        this.nodesExplored += nb;
    }

    public String getNodesExplored() {
        Integer a = this.nodesExplored + this.depth;
        return a.toString();
    }

    public Move getBestMove() {
        return this.bestMove;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void stopAllThread() {
        if (this.fils != null && this.fils.isActive()) {
            this.fils.stopAllThread();
        }

        if (this.isActive) {
            this.isActive = false;
            this.executor.shutdownNow();
        }
    }
}
