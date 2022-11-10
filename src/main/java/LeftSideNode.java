/* Basic Node Class. */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.move.Move;

public class LeftSideNode {
    
    private Board board;
    private Integer depth;
    private Double alpha;
    private Double beta;
    private Boolean playerToMaximize;
    private Integer nodesExplored;
    private Double score;

    private Move bestMove;

    private List<Move> children;

    public LeftSideNode(Board board, Integer depth, Double alpha, Double beta, Boolean playerToMaximise) {
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
        this.bestMove = this.children.get(0);
    }

    public Result PVS() {
        // Node la plus à Gauche
        if (this.depth == 3 || this.board.isDraw() || this.board.isMated() || this.board.isStaleMate()){
            Node n = new Node(this.board, this.depth, this.playerToMaximize, this.bestMove, this);
            Result r = n.alphaBetaCutOff(this.board, this.depth, this.alpha, this.beta, playerToMaximize);

            //System.out.println("First move : " + r.getBestMove().toString() + " | score : " + r.getNum());

            return r;
        }

        // On trouve le noeud le plus à gauche
        this.incrementNodesCount(1);
        this.board.doMove(this.bestMove);
        LeftSideNode fils = new LeftSideNode(this.board, this.depth - 1, this.alpha, this.beta, !this.playerToMaximize);
        Result r = fils.PVS();
        this.score = r.getNum();
        this.incrementNodesCount(r.getNodeExplored());;
        this.board.undoMove();

        // Crée une pool de (18) Thread
        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Future<Result>> resultList = new ArrayList<Future<Result>>();

        // Chacun des noeuds va exécuter Alpha Beta Cut Off
        for (Move m : this.children.subList(1, this.children.size())) {
            this.incrementNodesCount(1);
            board.doMove(m);
            Node n = new Node(this.board, this.depth - 1, !this.playerToMaximize, m, this);
            board.undoMove();
            resultList.add(executor.submit(n));
        }

        // Attendre que tous les Nodes donnent un résultat
        executor.shutdown();
        
        for (Future<Result> future : resultList) {
            try {     
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
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
            
        //System.out.println("Final bestMove : " + this.bestMove + " | score : " + this.score);

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
}
