import com.github.bhlangonijr.chesslib.move.Move;

/**
 * Record permettant de stocker le résultat d'une recherche
 * @param num : evaluation
 * @param bestMove : meilleur coup trouvé
 * @param nodeExplored : nombre de noeuds explorés
 */
public record Result(Double num, Move bestMove, Integer nodeExplored) {}
