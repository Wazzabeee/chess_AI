/* Evaluation Class. */

import com.github.bhlangonijr.chesslib.*;

import static java.lang.Long.bitCount;
import static java.lang.Math.min;

/**
 * Provide set of tools to evaluate a given position
 */


public class Evaluator {

    private static final long PAWN_VALUE = 100L;
    private static final long BISHOP_VALUE = 320L;
    private static final long KNIGHT_VALUE = 315L;
    private static final long ROOK_VALUE = 500L;
    private static final long QUEEN_VALUE = 900L;
    private static final long MATE_VALUE = 39000L;

    private static final long MAX_MATERIAL = PAWN_VALUE * 8 + KNIGHT_VALUE * 2 + BISHOP_VALUE * 2 + ROOK_VALUE * 2 + QUEEN_VALUE;
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

    private static final short[] RookTable = new short[]
            {
                    0, 0, 0, 0, 0, 0, 0, 0,
                    5, 10, 10, 10, 10, 10, 10, 5,
                    -5, 0, 0, 0, 0, 0, 0, -5,
                    -5, 0, 0, 0, 0, 0, 0, -5,
                    -5, 0, 0, 0, 0, 0, 0, -5,
                    -5, 0, 0, 0, 0, 0, 0, -5,
                    -5, 0, 0, 0, 0, 0, 0, -5,
                    0, 0, 0, 5, 5, 0, 0, 0
            };

    private static final short[] QueenTable = new short[]
            {
                    -20, -10, -10, -5, -5, -10, -10, -20,
                    -10, 0, 0, 0, 0, 0, 0, -10,
                    -10, 0, 5, 5, 5, 5, 0, -10,
                    -5, 0, 5, 5, 5, 5, 0, -5,
                    0, 0, 5, 5, 5, 5, 0, -5,
                    -10, 5, 5, 5, 5, 5, 0, -10,
                    -10, 0, 5, 0, 0, 0, 0, -10,
                    -20, -10, -10, -5, -5, -10, -10, -20
            };
    private static final short[] KingOpeningTable = new short[]
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
    private static final short[] KingEndingTable = new short[]
            {
                    -50, -40, -30, -20, -20, -30, -40, -50,
                    -30, -20, -10, 0, 0, -10, -20, -30,
                    -30, -10, 20, 30, 30, 20, -10, -30,
                    -30, -10, 30, 40, 40, 30, -10, -30,
                    -30, -10, 30, 40, 40, 30, -10, -30,
                    -30, -10, 20, 30, 30, 20, -10, -30,
                    -30, -30, 0, 0, 0, 0,-30,-30,
                    -50, -30,-30,-30,-30,-30,-30,-50
            };

    /**
     * Calcule le score des deux joueurs basé sur la valeur des pièces
     *
     * @param b : etat du jeu actuel
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

        long materialSide = scoreMaterial(b, Side.WHITE);
        long materialOtherSide = scoreMaterial(b, Side.BLACK);
        long scorePieceSquares = scorePieceSquare(b);

        /*int m = (white) ? lastPlayerMoves : b.legalMoves().size();
        int M = (white) ? b.legalMoves().size() : lastPlayerMoves;*/
        return (materialSide - materialOtherSide) + scorePieceSquares; //+ (M - m);
    }

    public static long getPieceStaticValue(Piece p)
    {
        switch (p.getPieceType()) {
            case PAWN -> {
                return PAWN_VALUE;
            }
            case KNIGHT -> {
                return KNIGHT_VALUE;
            }
            case BISHOP -> {
                return BISHOP_VALUE;
            }
            case ROOK -> {
                return ROOK_VALUE;
            }
            case QUEEN -> {
                return QUEEN_VALUE;
            }
            case KING -> {
                return MATE_VALUE;
            }
            default -> {
                return 0L;
            }
        }
    }
    public static long getSquareStaticValue(Piece p, Square s)
    {
        switch (p.getPieceType()) {
            case PAWN -> {
                return PawnTable[getIndex(p.getPieceSide(), s)];
            }
            case KNIGHT -> {
                return KnightTable[getIndex(p.getPieceSide(), s)];
            }
            case BISHOP -> {
                return BishopTable[getIndex(p.getPieceSide(), s)];
            }
            case ROOK -> {
                return RookTable[getIndex(p.getPieceSide(), s)];
            }
            case QUEEN -> {
                return QueenTable[getIndex(p.getPieceSide(), s)];
                //return 0L;
            }
            case KING -> {
                return KingEndingTable[getIndex(p.getPieceSide(), s)];
            }
            default -> {
                return 0L;
            }
        }
    }

    private static long scoreMaterial(Board b, Side s) {
        return countMaterial(b, s) - countMaterial(b, s.flip());
    }

    private static long scorePieceSquare(Board b) {
        return calculatePieceSquare(b, Side.WHITE, scoreMaterial(b, Side.WHITE)) -
                calculatePieceSquare(b, Side.BLACK, scoreMaterial(b, Side.BLACK));
    }

    private static long calculatePieceSquare(Board b, Side sideToMove, Long materialSide) {
        long phase = min(MAX_MATERIAL, materialSide);
        long somme = 0L;

        long pieces = b.getBitboard(sideToMove) & ~ b.getBitboard(Piece.make(sideToMove, PieceType.KING));

        while (pieces != 0L)
        {
            int index = Bitboard.bitScanForward(pieces);
            pieces = Bitboard.extractLsb(pieces);
            Square sq = Square.squareAt(index);
            somme += getSquareStaticValue(b.getPiece(sq), sq);
        }

        for (Square sq : b.getPieceLocation(Piece.make(sideToMove, PieceType.KING)))
        {
            somme += (MAX_MATERIAL - phase) * KingOpeningTable[getIndex(sideToMove, sq)] /
                    MAX_MATERIAL + phase * KingEndingTable[getIndex(sideToMove, sq)] / MAX_MATERIAL;
        }
        return somme;
    }



    private static int getIndex(Side side, Square sq)
    {
        return (side == Side.BLACK) ? sq.ordinal() : 63 - sq.ordinal();
    }

    private static long countMaterial(Board b, Side s)
    {
        return (bitCount(b.getBitboard(Piece.make(s, PieceType.PAWN))) * PAWN_VALUE +
                bitCount(b.getBitboard(Piece.make(s, PieceType.BISHOP))) * BISHOP_VALUE +
                bitCount(b.getBitboard(Piece.make(s, PieceType.KNIGHT))) * KNIGHT_VALUE +
                bitCount(b.getBitboard(Piece.make(s, PieceType.ROOK))) * ROOK_VALUE +
                bitCount(b.getBitboard(Piece.make(s, PieceType.QUEEN))) * QUEEN_VALUE);
    }

}

