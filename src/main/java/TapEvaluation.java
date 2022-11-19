import com.github.bhlangonijr.chesslib.*;

import static java.lang.Long.bitCount;

public class TapEvaluation {

    private static final int PHASE_CONSTANT = 256;

    private static final long PAWN_VALUE = 100L;
    private static final long BISHOP_VALUE = 320L;
    private static final long KNIGHT_VALUE = 315L;
    private static final long ROOK_VALUE = 500L;
    private static final long QUEEN_VALUE = 900L;
    private static final long MATE_VALUE = 39000L;
    private static final int KNIGHT_PENALTY =  -10;
    private static final int ROOK_PENALTY = -20;
    private static final int NO_PAWNS_PENALTY = -20;

    private static final int TEMPO_BONUS = 10;

    private static final int[] KNIGHT_PAWN_ADJUSTMENT =
            {-30, -20, -15, -10, -5, 0, 5, 10, 15};
    private static final int[] ROOK_PAWN_ADJUSTMENT =
            {25, 20, 15, 10, 5, 0, -5, -10, -15};
    private static final int[] DUAL_BISHOP_ADJUSTMENT =
            {40, 40, 35, 30, 25, 20, 20, 15, 15};

    private static final short[] PawnTable =
            new short[]
                    {
                            0,  0,  0,  0,  0,  0,  0,  0,
                            50, 50, 50, 50, 50, 50, 50, 50,
                            10, 10, 20, 30, 30, 20, 10, 10,
                            5,  5,  10, 27, 27, 10,  5,  5,
                            0,  0,  0,  25, 25, 0,   0,  0,
                            5, -5, -10,  0, 0, -10, -5,  5,
                            5, 10,  10,-25,-25, 10, 10,  5,
                            0,  0,  0,   0, 0,   0,  0,  0
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
                    -20,-10,-10,-10,-10,-10,-10,-20,
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

    public static double eval(Board b)
    {
        if (b.isInsufficientMaterial() || (b.getHalfMoveCounter() >= 100 && b.isStaleMate())) // if draw return 0
        {
            return 0.0;
        } else if (b.isMated() && (b.getSideToMove() == Side.WHITE)) // if mate and white to play then -inf
        {
            return -MATE_VALUE;
        } else if (b.isMated() && (b.getSideToMove() != Side.WHITE)) //if mate and black to play then +inf
        {
            return MATE_VALUE;
        }

        double openingWhiteValue = countMaterial(b, Side.WHITE) + calculatePieceSquare(b, Side.WHITE);
        double endingWhiteValue = openingWhiteValue + KingEndingTable[getIndex(Side.WHITE,
                b.getPieceLocation(Piece.make(Side.WHITE, PieceType.KING)).get(0))];

        double openingBlackValue = countMaterial(b, Side.BLACK) + calculatePieceSquare(b, Side.BLACK);
        double endingBlackValue = openingBlackValue + KingEndingTable[getIndex(Side.BLACK,
                b.getPieceLocation(Piece.make(Side.BLACK, PieceType.KING)).get(0))];

        int phase = getPhase(b);

        double whiteValue = ((openingWhiteValue * (PHASE_CONSTANT - phase)) +
                (endingWhiteValue * phase)) / PHASE_CONSTANT;

        double blackValue = ((openingBlackValue * (PHASE_CONSTANT - phase)) +
                (endingBlackValue * phase)) / PHASE_CONSTANT;


        // Return the difference between our current score and opponents
        if (!b.isRepetition())
            return whiteValue - blackValue;

        return (whiteValue - blackValue > 0) ? -MATE_VALUE : 0.0;
    }

    private static int getPhase(Board b) {
        int knightPhase = 1;
        int bishopPhase = 1;
        int rookPhase = 2;
        int queenPhase = 4;
        int totalPhase = knightPhase*4 + bishopPhase*4 + rookPhase*4 + queenPhase*2;
        int phase = totalPhase;

        phase -= bitCount(b.getBitboard(Piece.make(Side.WHITE, PieceType.KNIGHT))) * knightPhase;
        phase -= bitCount(b.getBitboard(Piece.make(Side.BLACK, PieceType.KNIGHT))) * knightPhase;
        phase -= bitCount(b.getBitboard(Piece.make(Side.WHITE, PieceType.BISHOP))) * bishopPhase;
        phase -= bitCount(b.getBitboard(Piece.make(Side.BLACK, PieceType.BISHOP))) * bishopPhase;
        phase -= bitCount(b.getBitboard(Piece.make(Side.WHITE, PieceType.ROOK))) * rookPhase;
        phase -= bitCount(b.getBitboard(Piece.make(Side.BLACK, PieceType.ROOK))) * rookPhase;
        phase -= bitCount(b.getBitboard(Piece.make(Side.WHITE, PieceType.QUEEN))) * bishopPhase;
        phase -= bitCount(b.getBitboard(Piece.make(Side.BLACK, PieceType.QUEEN))) * bishopPhase;

        return (phase * PHASE_CONSTANT + (totalPhase / 2)) / totalPhase;
    }

    public static long getSquareStaticValue(Piece p, Square s)
    {
        PieceType pt = p.getPieceType();

        if(pt == PieceType.PAWN)
            return PawnTable[getIndex(p.getPieceSide(), s)];
        if(pt == PieceType.KNIGHT)
            return KnightTable[getIndex(p.getPieceSide(), s)];
        if(pt == PieceType.BISHOP)
            return BishopTable[getIndex(p.getPieceSide(), s)];
        if(pt == PieceType.ROOK)
            return RookTable[getIndex(p.getPieceSide(), s)];
        if(pt == PieceType.QUEEN)
            return QueenTable[getIndex(p.getPieceSide(), s)];
        if(pt == PieceType.KING)
            return KingEndingTable[getIndex(p.getPieceSide(), s)];

        return 0L;
    }

    private static long calculatePieceSquare(Board b, Side sideToMove) {

        long somme = 0L;
        long pieces = b.getBitboard(sideToMove) & ~ b.getBitboard(Piece.make(sideToMove, PieceType.KING));
        int knightCount, bishopCount, rookCount, pawnCount, opponentPawnCount, queenCount;
        knightCount = bishopCount = rookCount = pawnCount = queenCount = 0;
        Side opponentSide = (sideToMove == Side.WHITE) ? Side.BLACK : Side.WHITE;

        while (pieces != 0L)
        {
            int index = Bitboard.bitScanForward(pieces);
            pieces = Bitboard.extractLsb(pieces);
            Square sq = Square.squareAt(index);
            Piece currentPiece = b.getPiece(sq);

            PieceType pt = currentPiece.getPieceType();

            if(pt == PieceType.PAWN)
                pawnCount += 1;
            if(pt == PieceType.KNIGHT)
                knightCount += 1;
            if(pt == PieceType.BISHOP)
                bishopCount += 1;
            if(pt == PieceType.ROOK)
                rookCount += 1;
            if(pt == PieceType.QUEEN)
                queenCount += 1;

            somme += getSquareStaticValue(currentPiece, sq);
        }

        somme += KingOpeningTable[getIndex(sideToMove,
                b.getPieceLocation(Piece.make(sideToMove, PieceType.KING)).get(0))];

        if (knightCount > 0)
            somme += (long) KNIGHT_PAWN_ADJUSTMENT[pawnCount] * knightCount;

        if (rookCount > 0)
            somme += (long) ROOK_PAWN_ADJUSTMENT[pawnCount] * rookCount;

        if (bishopCount > 1)
            somme += DUAL_BISHOP_ADJUSTMENT[pawnCount];

        if (knightCount > 1)
            somme += KNIGHT_PENALTY;

        if (rookCount > 1)
            somme += ROOK_PENALTY;

        if (pawnCount == 0)
            somme += NO_PAWNS_PENALTY;

        // Program should not expect to win with very low material
        if ((pawnCount == 0) && (somme < BISHOP_VALUE) && (somme > 0)) {
            return 0;
        }

        opponentPawnCount = bitCount(b.getBitboard(Piece.make(opponentSide, PieceType.PAWN)));
        // Program should not except to win having only 2 knights and no pawns
        if (somme > 0 && pawnCount == 0 && opponentPawnCount == 0 && knightCount == 2 &&
                bishopCount == 0 && rookCount == 0 && queenCount == 0) {
            return 0;
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