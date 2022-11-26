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

// Le reste de l'arbre
public class Node implements Callable<Result> {

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
    }

    /**
     * Minimax à profondeur limitée avec élagage alpha beta "sans faille" (fail-soft)
     *
     * @param board : Position de jeu actuelle
     * @param depth : Profondeur restante de recherche
     * @param alpha : Borne alpha pour élagage
     * @param beta : Borne beta pour élagage
     * @param playerToMaximize : Booléen (Vrai = BLanc, Noir sinon)
     * @return : Record (Evaluation, bestMove, NodesExplored)
     */
    public Result alphaBetaCutOff(Board board, Integer depth, Double alpha, Double beta, Boolean playerToMaximize) {
        // Si feuille ou cas terminal
        if (depth == 0 || board.isDraw() || board.isMated() || board.isStaleMate()) {
            // Possibilité de modifier l'appel ci-dessous pour utiliser la Tapered Evaluation ou Quiescent Search
            return new Result(BasicEvaluation.evaluate(board), null, 0);
        } 

        // Génère la liste des mouvements possibles
        List<Move> children = board.legalMoves();

        // Trie du mouvement le plus intéressant au moins intéressant
        children.sort(Comparator.comparingInt((Move m) -> (int) getMoveScore(board, m)));
        Collections.reverse(children);

        Move bestMove = children.get(0); // Récupère un move au cas où il y a un problème plus bas

        if (playerToMaximize) { // If White
            Double maxEval = alpha;

            for (Move move : children) {
                incrementNodesCount(); // MAJ du nombre de noeuds

                board.doMove(move); // On effectue le coup directement sur le board (pas de copie)

                double currentEval = alphaBetaCutOff(board, depth - 1, alpha, beta, false).num();
                board.undoMove(); // On annule le coup

                if (maxEval < currentEval) { // Maj de l'eval + move
                    maxEval = currentEval;
                    bestMove = move;
                }

                alpha = max(alpha, maxEval);

                if (beta <= alpha) {
                    break; // Beta cut off
                }
            }

            return new Result(maxEval, bestMove, this.nodesExplored);

        } else { // Black player
            Double minEval = beta;

            for (Move move : children) {
                incrementNodesCount(); // MAJ du nombre de noeuds

                board.doMove(move); // On effectue le coup directement sur le board (pas de copie)
                double currentEval = alphaBetaCutOff(board, depth - 1, alpha, beta, true).num();
                board.undoMove(); // On annule le coup

                if (currentEval < minEval) { // MAJ de l'eval + move
                    minEval = currentEval;
                    bestMove = move;
                }

                beta = min(beta, minEval);

                if (beta <= alpha) {
                    break; // Alpha cut off
                }
            }

            return new Result(minEval, bestMove, this.nodesExplored);
        }
    }

    /**
     * Effectue les coups capturants afin d'évaluer une position dite calme ou discrète
     *
     * @param b : Position de jeu actuelle
     * @param alpha : Borne alpha pour élagage
     * @param beta : Borne beta pour élagage
     * @return double : Evaluation de la position
     */
    public double QuiescentSearch(Board b, Double alpha, Double beta)
    {
        double eval = BasicEvaluation.evaluate(b);

        if (eval >= beta)
            return beta;
        if (eval > alpha)
            return alpha;

        List<Move> captureMoves = b.pseudoLegalCaptures(); // Coups capturant
        for (Move move : captureMoves)
        {
            b.doMove(move);
            eval = -QuiescentSearch(b, -beta, -alpha); // On inverse le signe qui s'annule automatiquement 1 fois sur 2
            b.undoMove();

            if (eval >= beta)
                return beta;
            if (eval > alpha)
                alpha = eval;
        }
        return alpha;
    }

    /**
     * Evalue un coup en fonction de la pièce attaquée, de la promotion ou de la position finale de la pièce
     *
     * @param b : Etat du jeu actuel
     * @param move : Coup qui va potentiellement être joué
     * @return long : Score associé au coup
     */
    public static long getMoveScore(Board b, Move move)
    {
        Piece attackedPiece = b.getPiece(move.getTo());
        Piece attackingPiece = b.getPiece(move.getFrom());

        if (attackedPiece != Piece.NONE)
            return BasicEvaluation.getPieceStaticValue(attackedPiece);
        if (move.getPromotion() != Piece.NONE)
            return BasicEvaluation.getPieceStaticValue(move.getPromotion());

        return BasicEvaluation.getSquareStaticValue(attackingPiece, move.getTo());

    }

    private void incrementNodesCount() {
        this.nodesExplored++; // MAJ nombre de noeuds explorés
    }

    public Integer getNodesExplored() {
        return this.nodesExplored;
    }

    @Override
    public Result call() {
        Result r = this.alphaBetaCutOff(this.board, this.depth, this.alpha, this.beta, this.playerToMaximize);

        return new Result(r.num(), this.move, this.nodesExplored);
    }
}
