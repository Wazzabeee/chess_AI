import java.util.HashMap;
import java.util.Map;

/**
 * Provide set of tools to evaluate a given position
 */
public class Evaluator {

    /**
     * Calcule le score des deux joueurs basé sur la valeur des pièces
     *
     * @param fenString de la partie à évaluer
     * @return int : score de l'heuristique (>0 avantage blanc, <0 avantage noir, =0 : egal)
     */
    public static int scoresFromFen(String fenString)
    {
        // Hashmap to store number of pieces per category
        Map<Character, Integer> pieces = new HashMap<>();

        // String array to split fen by "/" character
        String[] parts = fenString.split("/");

        // Get information from last part of FEN String
        //String[] lastPart = parts[parts.length - 1].split("\\s+");

        // Update part last element to be compatible with next loop
        parts[parts.length - 1] = parts[parts.length - 1].substring(0, parts[parts.length - 1].indexOf(" "));

        for (String part: parts)
        {
            for (int i = 0, n = part.length(); i < n; i++)
            {
                char key = part.charAt(i);
                pieces.put(key, pieces.getOrDefault(key, 0) + 1);
            }
        }

        return 200 * (pieces.getOrDefault('K', 0) - pieces.getOrDefault('k', 0))
                + 9 * (pieces.getOrDefault('Q', 0) - pieces.getOrDefault('q', 0))
                + 5 * (pieces.getOrDefault('R', 0) - pieces.getOrDefault('r', 0))
                + 3 * (pieces.getOrDefault('B', 0) - pieces.getOrDefault('b', 0) +
                    pieces.getOrDefault('N', 0) - pieces.getOrDefault('n', 0))
                + (pieces.getOrDefault('P', 0) - pieces.getOrDefault('p', 0));
    }
}

