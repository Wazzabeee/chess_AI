
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Polyglot opening book support
 *
 * @author rui
 *
 * Borrowed from: https://github.com/albertoruibal/carballo
 */
public class readBook {
    // https://github.com/michaeldv/donna_opening_books
    // https://chessengines.blogspot.com/2021/01/new-chess-opening-book-m112-bin-and-ctg.html

    private final String bookName;

    List<Move> moves = new ArrayList<>();
    List<Short> weights = new ArrayList<>();
    long totalWeight;

    public readBook() {
        bookName = "book.bin";
    }

    /**
     * "move" is a bit field with the following meaning (bit 0 is the least significant bit)
     * <p/>
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

        long key2Find = fenToPolyglot.getKey(board.getFen());

        try {
            InputStream bookIs = getClass().getResourceAsStream(bookName);
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(bookIs));

            long key;
            short moveInt;
            short weight;
            while (true) {
                key = dataInputStream.readLong();

                if (key == key2Find) {
                    moveInt = dataInputStream.readShort();
                    weight = dataInputStream.readShort();
                    dataInputStream.readInt(); // Unused learn field

                    Move move = new Move(int2MoveString(moveInt), board.getSideToMove());
                    System.out.println(int2MoveString(moveInt) + " " + weight);

                    // Add only if it is legal
                    if (board.isMoveLegal(move, true)) {
                        moves.add(move);
                        weights.add(weight);
                        totalWeight += weight;
                        break;
                    }
                } else {
                    dataInputStream.skipBytes(8);
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Gets a random move from the book taking care of weights
     */
    public Move getMove(Board board) {
        generateMoves(board);
        if (!moves.isEmpty())
            return moves.get(0);
        else
            return null;
    }
}