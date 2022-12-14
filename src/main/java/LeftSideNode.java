/* LeftSideNode Class. */

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.lang.Math.min;

// Ce sont les noeuds le plus à gauche de l'arbre à savoir le meilleur move théorique
public class LeftSideNode {
    
    private final Board board;
    private final Integer depth;
    private final Double alpha;
    private final Double beta;
    private final Boolean playerToMaximize;
    private Integer nodesExplored;

    private Move bestMove;
    private final Stop stop;

    private final List<Move> children;

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
        this.children.sort(Comparator.comparingInt((Move m) -> (int) Node.getMoveScore(this.board, m)));
        Collections.reverse(this.children);

        // Meilleur Move
        try {
            this.bestMove = this.children.get(0);
        } catch (Exception e) {}

        this.isActive = false;
        this.fils = null;
    }

    public Result PVS() {
        // Node la plus à Gauche
        if (this.depth == 3 || this.board.isDraw() || this.board.isMated() || this.board.isStaleMate()){
            Node n = new Node(this.board, this.depth, this.playerToMaximize, this.bestMove, this);
            return n.alphaBetaCutOff(this.board, this.depth, this.alpha, this.beta, playerToMaximize);
        }

        // On trouve le noeud le plus à gauche
        this.incrementNodesCount(1);
        this.board.doMove(this.bestMove);
        LeftSideNode fils = new LeftSideNode(this.board, this.depth - 1, this.alpha, this.beta, !this.playerToMaximize, this.stop);
        this.fils = fils;

        Result r = fils.PVS();

        Double score = r.num();
        this.incrementNodesCount(r.nodeExplored());
        this.board.undoMove();

        if (this.stop.getStop() || this.children.size() == 1) {
            return new Result(score, this.bestMove, this.nodesExplored);
        }

        // Crée une pool de (15) Thread
        this.executor = Executors.newFixedThreadPool(min(15, this.children.size() - 1));
        List<Future<Result>> resultList = new ArrayList<>();

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
            // Impossible d'ajouter une tâche à la pool
            this.executor.shutdown();
        } 

        if (!this.isActive()) {
            return new Result(score, this.bestMove, this.nodesExplored);
        }

        try {        
            for (Future<Result> future : resultList) {    
                if (!this.isActive()) {
                    break;
                }

                // Attend le résultat 
                r = future.get();

                this.incrementNodesCount(r.nodeExplored());

                if (playerToMaximize) {
                    if (score < r.num()) {
                        score = r.num();
                        this.bestMove = r.bestMove();
                    }
                } else {
                    if (r.num() < score){
                        score = r.num();
                        this.bestMove = r.bestMove();
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        this.isActive = false;

        return new Result(score, this.bestMove, this.nodesExplored);
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
        int a = this.nodesExplored + this.depth;
        return Integer.toString(a);
    }

    public Move getBestMove() {
        return this.bestMove;
    }

    public boolean isActive() {
        return this.isActive;
    }

    // Timer Friendly
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
