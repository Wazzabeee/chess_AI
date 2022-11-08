/* Basic Node Class. */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.*;

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
    private Lock verrousAB;
    private Lock verrousS;

    private LeftSideNode fils;
    private List<Move> children;

    public LeftSideNode(Board board, Integer depth, Double alpha, Double beta, Boolean playerToMaximise) {
        this.board = board;
        this.depth = depth;
        this.playerToMaximize = playerToMaximise;
        this.nodesExplored = 0;
        this.alpha = alpha;
        this.beta = beta;
        this.verrousAB = new ReentrantLock();
        this.verrousS = new ReentrantLock();
    }

    public Result PVS() {
        // Cas Terminal
        if (this.depth == 0 || this.board.isDraw() || this.board.isMated() || this.board.isStaleMate()) {
            return new Result(Evaluator.scoresFromFen(this.board, 20, this.playerToMaximize), null, 0);
        }

        // Récupère les Moves et les tri
        this.children = this.board.legalMoves();
        this.children.sort(Comparator.comparingInt((Move m) -> (int) getMoveScore(this.board, m)));
        Collections.reverse(this.children);

        // Meilleur Move
        this.bestMove = this.children.get(0);

        /*System.out.println("Depth = " + this.depth);
        System.out.println("List des BestMove = " + this.children);
        System.out.println("Le véritable bestMove = " + this.getBestMove());*/

        // On trouve le noeud le plus à gauche
        this.incrementNodesCount(1);
        this.board.doMove(this.bestMove);
        this.fils = new LeftSideNode(this.board, this.depth - 1, this.alpha, this.beta, !this.playerToMaximize);
        Result r = this.fils.PVS();
        this.score = r.getNum();
        this.incrementNodesCount(r.getNodeExplored());;
        this.board.undoMove();
        
        // Crée une pool de (18) Thread
        ExecutorService executor = Executors.newFixedThreadPool(18);

        // Chacun des noeuds va exécuter Alpha Beta Cut Off
        List<Node> listNodes = new ArrayList<Node>();
        for (Move m : this.children.subList(1, this.children.size())) {
            this.incrementNodesCount(1);
            board.doMove(m);
            Node n = new Node(this.board, this.depth - 1, !this.playerToMaximize, m, this);
            board.undoMove();

            listNodes.add(n);                
        }

        List<Future<Result>> resultList = null;

        // Exécution de l'algorithme
        try {
            resultList = executor.invokeAll(listNodes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Attendre que tous les Nodes donnent un résultat
        executor.shutdown();
        
        // TODO : Mettre un verrous sur Alpha/Beta
        for (Future<Result> future : resultList) {
            try {     
                r = future.get(); 

                this.incrementNodesCount(r.getNodeExplored());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        
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

    public String getBestMove() {
        if (this.bestMove == null) {
            return " ";
        }

        return this.bestMove.toString();
    }

    public Double getAlpha() {
        return this.alpha;
    } 

    public void setAlpha(Double value) {
        this.verrousAB.lock();
        this.alpha = value;
        this.verrousAB.unlock();
    } 

    public Double getBeta() {
        return this.beta;
    }

    public void setBeta(Double value) {
        this.verrousAB.lock();
        this.beta = value;
        this.verrousAB.unlock();
    }

    public Double getScore() {
        return this.score;
    }

    public void setScore(Double value, Move m) {
        this.verrousS.lock();
        this.score = value;
        this.setBestMove(m);
        this.verrousS.unlock();
    }

    public void setBestMove(Move m) {
        this.bestMove = m;
    }

    private void incrementNodesCount(int nb) {
        this.nodesExplored += nb;
    }

    public String getNodesExplored() {
        Integer a = this.nodesExplored + this.depth;
        return a.toString();
    }
}
