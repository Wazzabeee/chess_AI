import java.util.HashMap;
import java.util.Map;

public class Evaluator {

    /**
     * Calcule le score des deux joueurs basé sur la valeur des pièces
     *
     * @param fenString de la partie à évaluer
     * @return int[whiteScore, blackScore] according to normal pieces valuation (P=1, N=B=3, R=5, Q=10)
     */
    public static int[] scoresFromFen(String fenString)
    {
        int[] ans = new int[2];
        // Hashmap to store number of pieces per category
        Map<Character, Integer> pieces = new HashMap<>();

        // String array to split fen by "/" character
        String[] parts = fenString.split("/");

        // Get information from last part of FEN String
        String[] lastPart = parts[parts.length - 1].split("\\s+");

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
        //white score
        ans[0] = pieces.get('P') + pieces.get('R') * 5 + pieces.get('N') * 3 + pieces.get('B') * 3 + pieces.get('Q');
        //black score
        ans[1] = pieces.get('p') + pieces.get('r') * 5 + pieces.get('n') * 3 + pieces.get('b') * 3 + pieces.get('q');
        return ans;
    }
}
