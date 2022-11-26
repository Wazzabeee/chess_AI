import com.github.bhlangonijr.chesslib.*;

import static java.lang.Long.bitCount;

/**
 * Fonction d'évaluation évolutive au fil de la partie
 */
public class TaperedEvaluation {

    private static final int PHASE_CONSTANT = 256; // Phase maximale (diminue au cours de la partie)

    // Valeur des pièces
    private static final long PAWN_VALUE = 100L;
    private static final long KNIGHT_VALUE = 315L;
    private static final long BISHOP_VALUE = 320L;
    private static final long ROOK_VALUE = 500L;
    private static final long QUEEN_VALUE = 900L;
    private static final long MATE_VALUE = 39000L;

    // Pénalités
    private static final int KNIGHT_PENALTY =  -10;
    private static final int ROOK_PENALTY = -20;
    private static final int NO_PAWNS_PENALTY = -20;

    // Bonus ou malus en fonction du nombre de pions en jeu
    private static final int[] KNIGHT_PAWN_ADJUSTMENT =
            {-30, -20, -15, -10, -5, 0, 5, 10, 15};
    private static final int[] ROOK_PAWN_ADJUSTMENT =
            {25, 20, 15, 10, 5, 0, -5, -10, -15};
    private static final int[] DUAL_BISHOP_ADJUSTMENT =
            {40, 40, 35, 30, 25, 20, 20, 15, 15};

    // Piece Square Tables
    private static final short[] PawnTable = new short[]
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

    public static double eval(Board b) {
        if (b.isInsufficientMaterial() || (b.getHalfMoveCounter() >= 100 && b.isStaleMate())) {
            // if draw return 0
            return 0.0;
        } else if (b.isMated() && (b.getSideToMove() == Side.WHITE)) { 
            // if mate and white to play then -inf
            return -MATE_VALUE;
        } else if (b.isMated() && (b.getSideToMove() != Side.WHITE)) { 
            //if mate and black to play then +inf
            return MATE_VALUE;
        }

        // Score des blancs selon les bonus et pénalités d'un début de partie
        double openingWhiteValue = countMaterial(b, Side.WHITE) + calculatePieceSquare(b, Side.WHITE);
        // Score des blancs selon les bonus et pénalités d'une fin de partie
        double endingWhiteValue = openingWhiteValue + KingEndingTable[getIndex(Side.WHITE,
                b.getPieceLocation(Piece.make(Side.WHITE, PieceType.KING)).get(0))];

        // Score des noirs selon les bonus et pénalités d'un début de partie
        double openingBlackValue = countMaterial(b, Side.BLACK) + calculatePieceSquare(b, Side.BLACK);
        // Score des noirs selon les bonus et pénalités d'une fin de partie
        double endingBlackValue = openingBlackValue + KingEndingTable[getIndex(Side.BLACK,
                b.getPieceLocation(Piece.make(Side.BLACK, PieceType.KING)).get(0))];

        int phase = getPhase(b); // MAJ de la phase

        // Calcul du score de chaque camp avec une plus grande importance pour le score d'ouverture ou de
        // fermeture en fonction de la valeur de la phase.
        double whiteValue = ((openingWhiteValue * (PHASE_CONSTANT - phase)) +
                (endingWhiteValue * phase)) / PHASE_CONSTANT;

        double blackValue = ((openingBlackValue * (PHASE_CONSTANT - phase)) +
                (endingBlackValue * phase)) / PHASE_CONSTANT;

        // Renvoie la différence entre les deux scores si ce n'est pas un nul par répétition
        if (!b.isRepetition()) {
            return whiteValue - blackValue;
        }

        // Si les blancs ont l'avantage on renvoie -MATE_VALUE pour éviter le nul, sinon on renvoie 0
        return (whiteValue - blackValue > 0) ? -MATE_VALUE : 0.0;
    }

    /**
     * Calcul de la phase en fonction des pièces encore en jeu
     * Inspiré de : <a href="https://www.chessprogramming.org/Tapered_Eval">...</a>
     *
     * @param b : Position actuelle
     * @return : int : Phase à jour
     */
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

    /**
     * Renvoi le score associé à la PST de la pièce
     *
     * @param p : Pièce
     * @param s : Case sur laquelle se trouve la pièce
     * @return Score positionnel associé à la Pièce p sur la case s
     */
    public static long getSquareStaticValue(Piece p, Square s) {
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
            }
            case KING -> {
                return KingEndingTable[getIndex(p.getPieceSide(), s)];
            }
            default -> {
                return 0L;
            }
        }
    }

    private static long calculatePieceSquare(Board b, Side sideToMove) {

        long somme = 0L;
        long pieces = b.getBitboard(sideToMove) & ~ b.getBitboard(Piece.make(sideToMove, PieceType.KING));

        // Variables pour compter au fil du parcours du bitboard le nombre de pièces par catégorie
        int knightCount, bishopCount, rookCount, pawnCount, opponentPawnCount, queenCount;
        knightCount = bishopCount = rookCount = pawnCount = queenCount = 0;
        Side opponentSide = (sideToMove == Side.WHITE) ? Side.BLACK : Side.WHITE;

        while (pieces != 0L) {
            // Scan Forward pour trouver l'index Least Significant 1 Bit
            int index = Bitboard.bitScanForward(pieces);

            // Extraction du Least Significant Bit
            pieces = Bitboard.extractLsb(pieces);

            Square sq = Square.squareAt(index); // Récupère la case échiquéenne
            Piece currentPiece = b.getPiece(sq);

            PieceType pt = currentPiece.getPieceType();
            // Suite de if plutôt que switch pour tester .JAR sur PC UQAC (Java 8...)
            if(pt == PieceType.PAWN) {
                pawnCount += 1;
            }
            if(pt == PieceType.KNIGHT) {
                knightCount += 1;
            }
            if(pt == PieceType.BISHOP) {
                bishopCount += 1;
            }
            if(pt == PieceType.ROOK) {
                rookCount += 1;
            }
            if(pt == PieceType.QUEEN) {
                queenCount += 1;
            }

            somme += getSquareStaticValue(currentPiece, sq); // PST score
        }

        somme += KingOpeningTable[getIndex(sideToMove,
                b.getPieceLocation(Piece.make(sideToMove, PieceType.KING)).get(0))];

        // Ajustement de la valeur des cavalier en fonction du nombre de pions en jeu
        if (knightCount > 0) {
            somme += (long) KNIGHT_PAWN_ADJUSTMENT[pawnCount] * knightCount;
        }
        // Ajustement de la valeur des tours en fonction du nombre de pions en jeu
        if (rookCount > 0) {
            somme += (long) ROOK_PAWN_ADJUSTMENT[pawnCount] * rookCount;
        }
        // Ajustement de la valeur d'une paire de fou en fonction du nombre de pions en jeu
        if (bishopCount > 1) {
            somme += DUAL_BISHOP_ADJUSTMENT[pawnCount];
        }

        // Autres pénalités
        if (knightCount > 1) {
            somme += KNIGHT_PENALTY;
        }
        if (rookCount > 1) {
            somme += ROOK_PENALTY;
        }
        if (pawnCount == 0) {
            somme += NO_PAWNS_PENALTY;
        }

        // L'IA ne doit pas s'attendre à gagner avec très peu de pièces
        if ((pawnCount == 0) && (somme < BISHOP_VALUE) && (somme > 0)) {
            return 0;
        }

        opponentPawnCount = bitCount(b.getBitboard(Piece.make(opponentSide, PieceType.PAWN)));
        // MAT impossible avec seulement 2 cavaliers et pas de pions
        if (somme > 0 && pawnCount == 0 && opponentPawnCount == 0 && knightCount == 2 &&
                bishopCount == 0 && rookCount == 0 && queenCount == 0) {
            return 0;
        }

        return somme;
    }

    /**
     * Renvoi à partir d'une case échiquéenne, l'index associé dans une PST
     *
     * @param side : Joueur actuel
     * @param sq : Case en notation échiquéenne
     * @return int : index dans la Piece Square Table
     */
    private static int getIndex(Side side, Square sq) {
        return (side == Side.BLACK) ? sq.ordinal() : 63 - sq.ordinal();
    }

    /**
     * Compte les bits correspondant à chacune des pièces du joueur s
     *
     * @param b : Etat du jeu actuel
     * @param s : Joueur actuel
     * @return long : score matériel du joueur
     */
    private static long countMaterial(Board b, Side s) {
        return (bitCount(b.getBitboard(Piece.make(s, PieceType.PAWN))) * PAWN_VALUE +
                bitCount(b.getBitboard(Piece.make(s, PieceType.BISHOP))) * BISHOP_VALUE +
                bitCount(b.getBitboard(Piece.make(s, PieceType.KNIGHT))) * KNIGHT_VALUE +
                bitCount(b.getBitboard(Piece.make(s, PieceType.ROOK))) * ROOK_VALUE +
                bitCount(b.getBitboard(Piece.make(s, PieceType.QUEEN))) * QUEEN_VALUE);
    }
}