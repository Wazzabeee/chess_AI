
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe permettant la lecture d'un livre d'ouvertures Polyglot
 * Code adapté de: <a href="https://github.com/albertoruibal/carballo">...</a>
 */
public class openingBook {

    private final String bookName;

    List<Move> moves = new ArrayList<>();
    List<Short> weights = new ArrayList<>();
    long totalWeight;

    public openingBook() {
        bookName = "book.bin";
    } // livre trouvable dans src/main/resources/book.bin

    /**
     * "move" is a bit field with the following meaning (bit 0 is the least significant bit)
     * bits                meaning
     * ===================================
     * 0,1,2               to file
     * 3,4,5               to row
     * 6,7,8               from file
     * 9,10,11             from row
     * 12,13,14            promotion piece
     * "promotion piece" is encoded as follows
     * none       0
     * knight     1
     * bishop     2
     * rook       3
     * queen      4
     *
     * @param move a move
     * @return string
     */
    private String int2MoveString(short move) {
        StringBuilder sb = new StringBuilder();
        sb.append((char) ('a' + ((move >> 6) & 0x7)));
        sb.append(((move >> 9) & 0x7) + 1);
        sb.append((char) ('a' + (move & 0x7)));
        sb.append(((move >> 3) & 0x7) + 1);
        if (((move >> 12) & 0x7) != 0) sb.append("NBRQ".charAt(((move >> 12) & 0x7) - 1));
        return sb.toString();
    }

    public void generateMoves(Board board) {
        totalWeight = 0;
        moves.clear();
        weights.clear();

        // traduit la position actuelle en clé à trouver dans le fichier binaire
        long key2Find = fenToPolyglot.getKey(board.getFen());

        try {
            InputStream bookIs = getClass().getResourceAsStream(bookName);
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(bookIs));

            long key;
            short moveInt;
            short weight;
            while (true) {
                key = dataInputStream.readLong();

                // Si on trouve une clé correspondante
                if (key == key2Find) {
                    moveInt = dataInputStream.readShort();
                    weight = dataInputStream.readShort();
                    dataInputStream.readInt(); // Unused learn field

                    // Traduction du move dans un format connu
                    Move move = new Move(int2MoveString(moveInt), board.getSideToMove());
                    System.out.println(int2MoveString(moveInt) + " " + weight);

                    // Ajoute ssi le move est légal
                    if (board.isMoveLegal(move, true)) {
                        moves.add(move);
                        weights.add(weight);
                        totalWeight += weight;
                        // on s'arrête directement pour gagner du temps et récupérer le premier coup
                        // les données sont triées, il s'agit donc du coup le plus joué dans le livre
                        break;
                    }
                } else {
                    dataInputStream.skipBytes(8); // on skip à la prochaine clé
                }
            }
        } catch (Exception e) {}
    }

    /**
     * Récupère le move le plus joué lors de l'ouverture à partir de la position actuelle
     */
    public Move getMove(Board board) {
        generateMoves(board);
        if (!moves.isEmpty())
            return moves.get(0);
        else
            return null; // Valeur qui va faire changer la valeur le booléen dans UCI.java (fin de l'opening)
    }
}