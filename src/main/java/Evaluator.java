import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;

import java.util.List;

/**
 * Provide set of tools to evaluate a given position
 */
public class Evaluator {

    private static final short[] PawnTable = new short[]
            {
                    0, 0, 0, 0, 0, 0, 0, 0,
                    50, 50, 50, 50, 50, 50, 50, 50,
                    10, 10, 20, 30, 30, 20, 10, 10,
                    5, 5, 10, 27, 27, 10, 5, 5,
                    0, 0, 0, 25, 25, 0, 0, 0,
                    5, -5,-10, 0, 0,-10, -5, 5,
                    5, 10, 10,-25,-25, 10, 10, 5,
                    0, 0, 0, 0, 0, 0, 0, 0
            };
    private static final short[] KnightTable = new short[]
            {
                    -50,-40,-30,-30,-30,-30,-40,-50,
                    -40,-20, 0, 0, 0, 0,-20,-40,
                    -30, 0, 10, 15, 15, 10, 0,-30,
                    -30, 5, 15, 20, 20, 15, 5,-30,
                    -30, 0, 15, 20, 20, 15, 0,-30,
                    -30, 5, 10, 15, 15, 10, 5,-30,
                    -40,-20, 0, 5, 5, 0,-20,-40,
                    -50,-40,-20,-30,-30,-20,-40,-50,
            };
    private static final short[] BishopTable = new short[]
            {
                    -20,-10,-10,-10,-10,-10,-10,-20,
                    -10, 0, 0, 0, 0, 0, 0,-10,
                    -10, 0, 5, 10, 10, 5, 0,-10,
                    -10, 5, 5, 10, 10, 5, 5,-10,
                    -10, 0, 10, 10, 10, 10, 0,-10,
                    -10, 10, 10, 10, 10, 10, 10,-10,
                    -10, 5, 0, 0, 0, 0, 5,-10,
                    -20,-10,-40,-10,-10,-40,-10,-20,
            };
    private static final short[] KingTable = new short[]
            {
                    -30, -40, -40, -50, -50, -40, -40, -30,
                    -30, -40, -40, -50, -50, -40, -40, -30,
                    -30, -40, -40, -50, -50, -40, -40, -30,
                    -30, -40, -40, -50, -50, -40, -40, -30,
                    -20, -30, -30, -40, -40, -30, -30, -20,
                    -10, -20, -20, -20, -20, -20, -20, -10,
                    20, 20, 0, 0, 0, 0, 20, 20,
                    20, 30, 10, 0, 0, 10, 30, 20
            };
    private static final short[] KingTableEndGame = new short[]
            {
                    -50, -40, -30, -20, -20, -30, -40, -50,
                    -30, -20, -10, 0, 0, -10, -20, -30,
                    -30, -10, 20, 30, 30, 20, -10, -30,
                    -30, -10, 30, 40, 40, 30, -10, -30,
                    -30, -30, 0, 0, 0, 0,-30,-30,
                    -50, -30,-30,-30,-30,-30,-30,-50
            };

    /**
     * Calcule le score des deux joueurs basé sur la valeur des pièces
     *
     * @param lastPlayerMoves : nombre de coups disponibles pour le dernier joueur à avoir joué
     * @param white : vrai si aux blancs de jouer, faux sinon
     * @return double : score de l'heuristique (>0 avantage blanc, <0 avantage noir, =0 : egal)
     */
    public static double scoresFromFen(Board b, Integer lastPlayerMoves, Boolean white)
    {
        if (b.isDraw()) // if draw return 0
        {
            return 0.0;
        } else if (b.isMated() && white) // if mate and white to play then -inf
        {
            return -Double.MAX_VALUE;
        } else if (b.isMated() && !white) //if mate and black to play then +inf
        {
            return Double.MAX_VALUE;
        }

        List<Square> positions;
        double Score = 0;
        int m = (white) ? lastPlayerMoves : b.legalMoves().size();
        int M = (white) ? b.legalMoves().size() : lastPlayerMoves;

        Score += M;
        Score -= m;
        Score += 32767 * b.getPieceLocation(Piece.WHITE_KING).size() + KingTable[(byte)63 -(b.getFistPieceLocation(Piece.WHITE_KING)).ordinal()];
        Score -= 32767 * b.getPieceLocation(Piece.BLACK_KING).size() + KingTable[(b.getFistPieceLocation(Piece.BLACK_KING)).ordinal()];
        Score += 975 * b.getPieceLocation(Piece.WHITE_QUEEN).size();
        Score -= 975 * b.getPieceLocation(Piece.BLACK_QUEEN).size();
        Score += 500 * b.getPieceLocation(Piece.WHITE_ROOK).size();
        Score -= 500 * b.getPieceLocation(Piece.BLACK_ROOK).size();

        positions = b.getPieceLocation(Piece.WHITE_BISHOP);
        for (Square square : positions)
        {
            Score += 325 + BishopTable[(byte)63 - square.ordinal()];
        }

        positions = b.getPieceLocation(Piece.BLACK_BISHOP);
        for (Square square : positions)
        {
            Score -= 325 + BishopTable[square.ordinal()];
        }

        positions = b.getPieceLocation(Piece.WHITE_KNIGHT);
        for (Square square : positions)
        {
            Score += 320 + KnightTable[(byte)63 - square.ordinal()];
        }

        positions = b.getPieceLocation(Piece.BLACK_KNIGHT);
        for (Square square : positions)
        {
            Score -= 320 + KnightTable[square.ordinal()];
        }

        positions = b.getPieceLocation(Piece.WHITE_PAWN);
        for (Square square : positions)
        {
            Score += 100 + PawnTable[(byte)63 - square.ordinal()];
        }

        positions = b.getPieceLocation(Piece.BLACK_PAWN);
        for (Square square : positions)
        {
            Score -= 100 + PawnTable[square.ordinal()];
        }

        return Score;
    }
}

