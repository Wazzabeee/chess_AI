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
     * @return int[whiteScore, blackScore] according to normal pieces valuation (P=1, N=B=3, R=5, Q=10)
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

        return pieces.getOrDefault('P', 0) +
                    pieces.getOrDefault('R', 0) * 5 +
                    pieces.getOrDefault('N', 0) * 3 +
                    pieces.getOrDefault('Q', 0) * 10
                -
                (pieces.getOrDefault('p', 0) +
                        pieces.getOrDefault('r', 0) * 5 +
                        pieces.getOrDefault('n', 0) * 3 +
                        pieces.getOrDefault('q', 0) * 10);
    }
}
