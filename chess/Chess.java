package chess;

import java.util.ArrayList;
//import java.util.List;

public class Chess {

    enum Player { white, black }

    // game state fields
    private static ReturnPiece[][] board;
    private static Player curTurn;

    private static boolean whiteKingMoved;
    private static boolean blackKingMoved;
    private static boolean whiteQueenRookMoved;
    private static boolean whiteKingRookMoved;
    private static boolean blackQueenRookMoved;
    private static boolean blackKingRookMoved;

    private static int enPassantTargetRow;
    private static int enPassantTargetCol;
    private static int enPassantPawnRow;
    private static int enPassantPawnCol;

    private static class ParsedMove {
        int srcRow;
        int srcCol;
        int dstRow;
        int dstCol;
        Character promotionChoice;
        boolean drawOffer;
    }

    private static class MoveCandidate {
        int srcRow;
        int srcCol;
        int dstRow;
        int dstCol;

        ReturnPiece movingPiece;
        ReturnPiece capturedPiece;
        int capturedRow;
        int capturedCol;

        boolean isCastling;
        ReturnPiece rookPiece;
        int rookSrcCol;
        int rookDstCol;

        boolean isEnPassant;
        boolean isDoublePawnPush;

        boolean isPromotion;
        ReturnPiece.PieceType promotionType;
        ReturnPiece.PieceType originalType;
    }

    /**
     * Plays the next move for whichever player has the turn.
     *
     * @param move String for next move, e.g. "a2 a3"
     *
     * @return A ReturnPlay instance that contains the result of the move.
     *         See the section "The Chess class" in the assignment description for details of
     *         the contents of the returned ReturnPlay instance.
     */
    public static ReturnPlay play(String move) {
        if (board == null) {
            start();
        }

        /* FILL IN THIS METHOD */
        ReturnPlay result = new ReturnPlay();
        // handle leading/trailing spaces around the whole move
        String trimmedMove = move.trim();

        // just testing if it moves
        if ("resign".equals(trimmedMove)) {
            result.piecesOnBoard = getPieces();
            result.message = curTurn == Player.white
                    ? ReturnPlay.Message.RESIGN_BLACK_WINS
                    : ReturnPlay.Message.RESIGN_WHITE_WINS;
            return result;
        }

        //parse
        ParsedMove parsed = parseMove(trimmedMove);
        if (parsed == null) {
            return illegalResult(result);
        }

        MoveCandidate candidate = buildLegalMove(
                parsed.srcRow,
                parsed.srcCol,
                parsed.dstRow,
                parsed.dstCol,
                parsed.promotionChoice,
                curTurn);

        if (candidate == null) {
            return illegalResult(result);
        }

        applyCandidate(candidate);
        updateStateAfterMove(candidate);

        // switching turns FILL IN!!
        Player nextPlayer = opposite(curTurn);
        curTurn = nextPlayer;

        ReturnPlay.Message message = null;
        if (parsed.drawOffer) {
            message = ReturnPlay.Message.DRAW;
        } else {
            boolean nextInCheck = isKingInCheck(nextPlayer);
            if (nextInCheck) {
                if (!hasAnyLegalMove(nextPlayer)) {
                    message = nextPlayer == Player.white
                            ? ReturnPlay.Message.CHECKMATE_BLACK_WINS
                            : ReturnPlay.Message.CHECKMATE_WHITE_WINS;
                } else {
                    message = ReturnPlay.Message.CHECK;
                }
            }
        }

        result.piecesOnBoard = getPieces();
        // FILL IN
        result.message = message;
        return result;

        /* FOLLOWING LINE IS A PLACEHOLDER TO MAKE COMPILER HAPPY */
        /* WHEN YOU FILL IN THIS METHOD, YOU NEED TO RETURN A ReturnPlay OBJECT */
    }

    /**
     * This method should reset the game, and start from scratch.
     */
    public static void start() {
        /* FILL IN THIS METHOD */
        board = new ReturnPiece[8][8];
        curTurn = Player.white;

        whiteKingMoved = false;
        blackKingMoved = false;
        whiteQueenRookMoved = false;
        whiteKingRookMoved = false;
        blackQueenRookMoved = false;
        blackKingRookMoved = false;

        clearEnPassant();

        // initialize black pieces
        board[0][0] = createPiece(ReturnPiece.PieceType.BR, 'a', 8);
        board[0][1] = createPiece(ReturnPiece.PieceType.BN, 'b', 8);
        board[0][2] = createPiece(ReturnPiece.PieceType.BB, 'c', 8);
        board[0][3] = createPiece(ReturnPiece.PieceType.BQ, 'd', 8);
        board[0][4] = createPiece(ReturnPiece.PieceType.BK, 'e', 8);
        board[0][5] = createPiece(ReturnPiece.PieceType.BB, 'f', 8);
        board[0][6] = createPiece(ReturnPiece.PieceType.BN, 'g', 8);
        board[0][7] = createPiece(ReturnPiece.PieceType.BR, 'h', 8);

        // initalize the black pawns row
        for (int c = 0; c < 8; c++) {
            board[1][c] = createPiece(ReturnPiece.PieceType.BP, (char) ('a' + c), 7);
        }

        // initialize the white pieces
        board[7][0] = createPiece(ReturnPiece.PieceType.WR, 'a', 1);
        board[7][1] = createPiece(ReturnPiece.PieceType.WN, 'b', 1);
        board[7][2] = createPiece(ReturnPiece.PieceType.WB, 'c', 1);
        board[7][3] = createPiece(ReturnPiece.PieceType.WQ, 'd', 1);
        board[7][4] = createPiece(ReturnPiece.PieceType.WK, 'e', 1);
        board[7][5] = createPiece(ReturnPiece.PieceType.WB, 'f', 1);
        board[7][6] = createPiece(ReturnPiece.PieceType.WN, 'g', 1);
        board[7][7] = createPiece(ReturnPiece.PieceType.WR, 'h', 1);

        // intialize the white pawns row
        for (int c = 0; c < 8; c++) {
            board[6][c] = createPiece(ReturnPiece.PieceType.WP, (char) ('a' + c), 2);
        }
    }

    private static ParsedMove parseMove(String move) {
        String[] tokens = move.split("\\s+");
        if (tokens.length < 2 || tokens.length > 4) {
            return null;
        }
        if (tokens[0].length() != 2 || tokens[1].length() != 2) {
            return null;
        }

        int srcCol = fileToCol(tokens[0].charAt(0));
        int srcRow = rankToRow(tokens[0].charAt(1));
        int dstCol = fileToCol(tokens[1].charAt(0));
        int dstRow = rankToRow(tokens[1].charAt(1));
        if (!inBounds(srcRow, srcCol) || !inBounds(dstRow, dstCol)) {
            return null;
        }

        Character promotionChoice = null;
        boolean drawOffer = false;
        // token 3/4 can be promotion piece and/or draw?
        for (int i = 2; i < tokens.length; i++) {
            String extra = tokens[i];
            if ("draw?".equals(extra)) {
                if (drawOffer) {
                    return null;
                }
                drawOffer = true;
                continue;
            }

            if (!isPromotionToken(extra)) {
                return null;
            }
            if (promotionChoice != null) {
                return null;
            }
            promotionChoice = Character.toUpperCase(extra.charAt(0));
        }

        ParsedMove parsed = new ParsedMove();
        parsed.srcRow = srcRow;
        parsed.srcCol = srcCol;
        parsed.dstRow = dstRow;
        parsed.dstCol = dstCol;
        parsed.promotionChoice = promotionChoice;
        parsed.drawOffer = drawOffer;
        return parsed;
    }

    private static MoveCandidate buildLegalMove(
            int srcRow,
            int srcCol,
            int dstRow,
            int dstCol,
            Character promotionChoice,
            Player player) {

        // no-op move is illegal
        if (srcRow == dstRow && srcCol == dstCol) {
            return null;
        }

        ReturnPiece piece = board[srcRow][srcCol];
        // check if there is a piece at source, illegal move check
        if (piece == null || ownerOf(piece) != player) {
            return null;
        }

        ReturnPiece dstPiece = board[dstRow][dstCol];
        // prevent taking your own piece, illegal move check
        if (dstPiece != null && ownerOf(dstPiece) == player) {
            return null;
        }
        if (dstPiece != null && isKing(dstPiece)) {
            return null;
        }

        MoveCandidate candidate;
        // castling is encoded as king source/destination squares
        if (isKing(piece) && isCastlingTarget(srcRow, srcCol, dstRow, dstCol, player)) {
            if (promotionChoice != null) {
                return null;
            }
            candidate = buildCastleCandidate(piece, srcRow, srcCol, dstRow, dstCol, player);
        } else if (isPawn(piece)) {
            candidate = buildPawnCandidate(piece, srcRow, srcCol, dstRow, dstCol, promotionChoice, player);
        } else {
            if (promotionChoice != null) {
                return null;
            }
            // check if the move is legal for the piece type, illegal move check
            if (!MoveRules.isLegal(board, piece, srcRow, srcCol, dstRow, dstCol)) {
                return null;
            }
            candidate = basicCandidate(piece, srcRow, srcCol, dstRow, dstCol, dstPiece);
        }

        if (candidate == null) {
            return null;
        }

        if (!isSafeFor(candidate, player)) {
            return null;
        }
        // turn order check
        return candidate;
    }

    private static MoveCandidate buildPawnCandidate(
            ReturnPiece piece,
            int srcRow,
            int srcCol,
            int dstRow,
            int dstCol,
            Character promotionChoice,
            Player player) {

        MoveCandidate candidate = basicCandidate(piece, srcRow, srcCol, dstRow, dstCol, board[dstRow][dstCol]);
        int dir = player == Player.white ? -1 : 1;
        int startRow = player == Player.white ? 6 : 1;
        int promotionRow = player == Player.white ? 0 : 7;
        int rowDiff = dstRow - srcRow;
        int colDiff = dstCol - srcCol;

        if (colDiff == 0 && rowDiff == dir && board[dstRow][dstCol] == null) {
            // legal single-step pawn push
        } else if (colDiff == 0 && srcRow == startRow && rowDiff == 2 * dir
                && board[srcRow + dir][srcCol] == null && board[dstRow][dstCol] == null) {
            // remember two-square pawn move for possible en passant next turn
            candidate.isDoublePawnPush = true;
        } else if (Math.abs(colDiff) == 1 && rowDiff == dir) {
            ReturnPiece target = board[dstRow][dstCol];
            if (target != null && ownerOf(target) != player) {
                candidate.capturedPiece = target;
                candidate.capturedRow = dstRow;
                candidate.capturedCol = dstCol;
            } else if (target == null
                    && dstRow == enPassantTargetRow
                    && dstCol == enPassantTargetCol
                    && srcRow == enPassantPawnRow
                    && dstCol == enPassantPawnCol) {
                // en passant capture: destination is empty, captured pawn is adjacent
                ReturnPiece epPawn = board[enPassantPawnRow][enPassantPawnCol];
                if (epPawn == null || !isPawn(epPawn) || ownerOf(epPawn) == player) {
                    return null;
                }
                candidate.isEnPassant = true;
                candidate.capturedPiece = epPawn;
                candidate.capturedRow = enPassantPawnRow;
                candidate.capturedCol = enPassantPawnCol;
            } else {
                return null;
            }
        } else {
            return null;
        }

        if (dstRow == promotionRow) {
            char promotion = promotionChoice == null ? 'Q' : promotionChoice;
            ReturnPiece.PieceType promoted = promotedType(player == Player.white, promotion);
            if (promoted == null) {
                return null;
            }
            candidate.isPromotion = true;
            candidate.promotionType = promoted;
        } else if (promotionChoice != null) {
            return null;
        }

        return candidate;
    }

    private static MoveCandidate buildCastleCandidate(
            ReturnPiece king,
            int srcRow,
            int srcCol,
            int dstRow,
            int dstCol,
            Player player) {

        int homeRow = player == Player.white ? 7 : 0;
        if (srcRow != homeRow || srcCol != 4 || dstRow != homeRow) {
            return null;
        }

        boolean kingSide = dstCol == 6;
        boolean queenSide = dstCol == 2;
        if (!kingSide && !queenSide) {
            return null;
        }

        if (player == Player.white && whiteKingMoved) {
            return null;
        }
        if (player == Player.black && blackKingMoved) {
            return null;
        }

        int rookSrcCol = kingSide ? 7 : 0;
        int rookDstCol = kingSide ? 5 : 3;
        ReturnPiece rook = board[homeRow][rookSrcCol];
        if (rook == null || ownerOf(rook) != player || !isRook(rook)) {
            return null;
        }

        if (player == Player.white && kingSide && whiteKingRookMoved) {
            return null;
        }
        if (player == Player.white && queenSide && whiteQueenRookMoved) {
            return null;
        }
        if (player == Player.black && kingSide && blackKingRookMoved) {
            return null;
        }
        if (player == Player.black && queenSide && blackQueenRookMoved) {
            return null;
        }

        int[] between = kingSide ? new int[] {5, 6} : new int[] {1, 2, 3};
        for (int col : between) {
            if (board[homeRow][col] != null) {
                return null;
            }
        }

        Player opponent = opposite(player);
        if (isKingInCheck(player)) {
            return null;
        }
        int[] kingPath = kingSide ? new int[] {5, 6} : new int[] {3, 2};
        for (int col : kingPath) {
            if (isSquareAttacked(homeRow, col, opponent)) {
                return null;
            }
        }

        MoveCandidate candidate = new MoveCandidate();
        candidate.srcRow = srcRow;
        candidate.srcCol = srcCol;
        candidate.dstRow = dstRow;
        candidate.dstCol = dstCol;
        candidate.movingPiece = king;
        candidate.originalType = king.pieceType;
        candidate.capturedPiece = null;
        candidate.capturedRow = dstRow;
        candidate.capturedCol = dstCol;
        candidate.isCastling = true;
        candidate.rookPiece = rook;
        candidate.rookSrcCol = rookSrcCol;
        candidate.rookDstCol = rookDstCol;
        return candidate;
    }

    private static MoveCandidate basicCandidate(
            ReturnPiece piece,
            int srcRow,
            int srcCol,
            int dstRow,
            int dstCol,
            ReturnPiece captured) {

        MoveCandidate candidate = new MoveCandidate();
        candidate.srcRow = srcRow;
        candidate.srcCol = srcCol;
        candidate.dstRow = dstRow;
        candidate.dstCol = dstCol;
        candidate.movingPiece = piece;
        candidate.originalType = piece.pieceType;
        candidate.capturedPiece = captured;
        candidate.capturedRow = dstRow;
        candidate.capturedCol = dstCol;
        return candidate;
    }

    private static boolean isSafeFor(MoveCandidate candidate, Player player) {
        applyCandidate(candidate);
        boolean safe = !isKingInCheck(player);
        undoCandidate(candidate);
        return safe;
    }

    private static void applyCandidate(MoveCandidate candidate) {
        if (candidate.isCastling) {
            int row = candidate.srcRow;
            board[row][candidate.srcCol] = null;
            board[row][candidate.dstCol] = candidate.movingPiece;
            board[row][candidate.rookSrcCol] = null;
            board[row][candidate.rookDstCol] = candidate.rookPiece;

            updatePieceLocation(candidate.movingPiece, colToFile(candidate.dstCol), rowToRank(row));
            updatePieceLocation(candidate.rookPiece, colToFile(candidate.rookDstCol), rowToRank(row));
            return;
        }

        board[candidate.srcRow][candidate.srcCol] = null;
        if (candidate.isEnPassant) {
            board[candidate.capturedRow][candidate.capturedCol] = null;
        }

        board[candidate.dstRow][candidate.dstCol] = candidate.movingPiece;
        updatePieceLocation(candidate.movingPiece, colToFile(candidate.dstCol), rowToRank(candidate.dstRow));

        if (candidate.isPromotion) {
            // apply promotion on the destination square
            candidate.movingPiece.pieceType = candidate.promotionType;
        }
    }

    private static void undoCandidate(MoveCandidate candidate) {
        if (candidate.isCastling) {
            int row = candidate.srcRow;
            board[row][candidate.srcCol] = candidate.movingPiece;
            board[row][candidate.dstCol] = null;
            board[row][candidate.rookSrcCol] = candidate.rookPiece;
            board[row][candidate.rookDstCol] = null;

            updatePieceLocation(candidate.movingPiece, colToFile(candidate.srcCol), rowToRank(row));
            updatePieceLocation(candidate.rookPiece, colToFile(candidate.rookSrcCol), rowToRank(row));
            return;
        }

        if (candidate.isPromotion) {
            candidate.movingPiece.pieceType = candidate.originalType;
        }

        board[candidate.srcRow][candidate.srcCol] = candidate.movingPiece;
        updatePieceLocation(candidate.movingPiece, colToFile(candidate.srcCol), rowToRank(candidate.srcRow));

        if (candidate.isEnPassant) {
            board[candidate.dstRow][candidate.dstCol] = null;
            board[candidate.capturedRow][candidate.capturedCol] = candidate.capturedPiece;
        } else {
            board[candidate.dstRow][candidate.dstCol] = candidate.capturedPiece;
        }
    }

    private static void updateStateAfterMove(MoveCandidate candidate) {
        // once king/rook moved or captured from home squares, castling rights are gone
        updateCastlingRightsForMove(candidate.movingPiece, candidate.srcRow, candidate.srcCol);
        if (candidate.isCastling) {
            updateCastlingRightsForMove(candidate.rookPiece, candidate.srcRow, candidate.rookSrcCol);
        }
        if (candidate.capturedPiece != null) {
            updateCastlingRightsForCapture(candidate.capturedPiece, candidate.capturedRow, candidate.capturedCol);
        }

        clearEnPassant();
        if (candidate.isDoublePawnPush) {
            // en passant is only available immediately on the next move
            enPassantTargetRow = (candidate.srcRow + candidate.dstRow) / 2;
            enPassantTargetCol = candidate.srcCol;
            enPassantPawnRow = candidate.dstRow;
            enPassantPawnCol = candidate.dstCol;
        }
    }

    private static void updateCastlingRightsForMove(ReturnPiece piece, int srcRow, int srcCol) {
        switch (piece.pieceType) {
            case WK -> whiteKingMoved = true;
            case BK -> blackKingMoved = true;
            case WR -> {
                if (srcRow == 7 && srcCol == 0) {
                    whiteQueenRookMoved = true;
                }
                if (srcRow == 7 && srcCol == 7) {
                    whiteKingRookMoved = true;
                }
            }
            case BR -> {
                if (srcRow == 0 && srcCol == 0) {
                    blackQueenRookMoved = true;
                }
                if (srcRow == 0 && srcCol == 7) {
                    blackKingRookMoved = true;
                }
            }
            default -> { }
        }
    }

    private static void updateCastlingRightsForCapture(ReturnPiece captured, int row, int col) {
        switch (captured.pieceType) {
            case WR -> {
                if (row == 7 && col == 0) {
                    whiteQueenRookMoved = true;
                }
                if (row == 7 && col == 7) {
                    whiteKingRookMoved = true;
                }
            }
            case BR -> {
                if (row == 0 && col == 0) {
                    blackQueenRookMoved = true;
                }
                if (row == 0 && col == 7) {
                    blackKingRookMoved = true;
                }
            }
            default -> { }
        }
    }

    private static boolean hasAnyLegalMove(Player player) {
        // brute force all destination squares and stop on the first legal move
        for (int srcRow = 0; srcRow < 8; srcRow++) {
            for (int srcCol = 0; srcCol < 8; srcCol++) {
                ReturnPiece piece = board[srcRow][srcCol];
                if (piece == null || ownerOf(piece) != player) {
                    continue;
                }

                for (int dstRow = 0; dstRow < 8; dstRow++) {
                    for (int dstCol = 0; dstCol < 8; dstCol++) {
                        if (srcRow == dstRow && srcCol == dstCol) {
                            continue;
                        }

                        if (isPawn(piece) && dstRow == (player == Player.white ? 0 : 7)) {
                            // promotion destination needs per-piece validation (Q/R/B/N)
                            char[] promotions = {'Q', 'R', 'B', 'N'};
                            for (char promotion : promotions) {
                                MoveCandidate candidate = buildLegalMove(
                                        srcRow, srcCol, dstRow, dstCol, promotion, player);
                                if (candidate != null) {
                                    return true;
                                }
                            }
                        } else {
                            MoveCandidate candidate = buildLegalMove(
                                    srcRow, srcCol, dstRow, dstCol, null, player);
                            if (candidate != null) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean isKingInCheck(Player player) {
        ReturnPiece.PieceType kingType = player == Player.white
                ? ReturnPiece.PieceType.WK
                : ReturnPiece.PieceType.BK;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ReturnPiece piece = board[row][col];
                if (piece != null && piece.pieceType == kingType) {
                    return isSquareAttacked(row, col, opposite(player));
                }
            }
        }

        return false;
    }

    private static boolean isSquareAttacked(int targetRow, int targetCol, Player byPlayer) {
        for (int srcRow = 0; srcRow < 8; srcRow++) {
            for (int srcCol = 0; srcCol < 8; srcCol++) {
                ReturnPiece piece = board[srcRow][srcCol];
                if (piece == null || ownerOf(piece) != byPlayer) {
                    continue;
                }
                if (attacksSquare(piece, srcRow, srcCol, targetRow, targetCol)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean attacksSquare(
            ReturnPiece piece,
            int srcRow,
            int srcCol,
            int targetRow,
            int targetCol) {

        int rowDiff = targetRow - srcRow;
        int colDiff = targetCol - srcCol;
        int absRow = Math.abs(rowDiff);
        int absCol = Math.abs(colDiff);

        return switch (piece.pieceType) {
            case WP -> rowDiff == -1 && absCol == 1;
            case BP -> rowDiff == 1 && absCol == 1;
            case WN, BN -> (absRow == 2 && absCol == 1) || (absRow == 1 && absCol == 2);
            case WB, BB -> absRow == absCol && absRow > 0 && pathClear(srcRow, srcCol, targetRow, targetCol);
            case WR, BR -> (srcRow == targetRow || srcCol == targetCol)
                    && (srcRow != targetRow || srcCol != targetCol)
                    && pathClear(srcRow, srcCol, targetRow, targetCol);
            case WQ, BQ -> (((srcRow == targetRow || srcCol == targetCol) && (srcRow != targetRow || srcCol != targetCol))
                    || (absRow == absCol && absRow > 0))
                    && pathClear(srcRow, srcCol, targetRow, targetCol);
            case WK, BK -> Math.max(absRow, absCol) == 1;
        };
    }

    private static boolean pathClear(int srcRow, int srcCol, int dstRow, int dstCol) {
        int rowStep = Integer.compare(dstRow, srcRow);
        int colStep = Integer.compare(dstCol, srcCol);
        int row = srcRow + rowStep;
        int col = srcCol + colStep;

        while (row != dstRow || col != dstCol) {
            if (board[row][col] != null) {
                return false;
            }
            row += rowStep;
            col += colStep;
        }
        return true;
    }

    private static ReturnPlay illegalResult(ReturnPlay result) {
        result.piecesOnBoard = getPieces();
        result.message = ReturnPlay.Message.ILLEGAL_MOVE;
        return result;
    }

    // helper method to make pieces for start() method
    private static ReturnPiece createPiece(ReturnPiece.PieceType type, char file, int rank) {
        ReturnPiece piece = new ReturnPiece();
        piece.pieceType = type;
        piece.pieceFile = ReturnPiece.PieceFile.valueOf(String.valueOf(file));
        piece.pieceRank = rank;
        return piece;
    }

    private static ArrayList<ReturnPiece> getPieces() {
        ArrayList<ReturnPiece> pieces = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board[row][col] != null) {
                    pieces.add(board[row][col]);
                }
            }
        }
        return pieces;
    }

    private static void clearEnPassant() {
        enPassantTargetRow = -1;
        enPassantTargetCol = -1;
        enPassantPawnRow = -1;
        enPassantPawnCol = -1;
    }

    private static boolean inBounds(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    private static int fileToCol(char file) {
        return file - 'a';
    }

    private static int rankToRow(char rank) {
        return 8 - (rank - '0');
    }

    private static char colToFile(int col) {
        return (char) ('a' + col);
    }

    private static char rowToRank(int row) {
        return (char) ('8' - row);
    }

    private static boolean isPromotionToken(String token) {
        if (token.length() != 1) {
            return false;
        }
        char piece = Character.toUpperCase(token.charAt(0));
        return piece == 'Q' || piece == 'R' || piece == 'B' || piece == 'N';
    }

    private static ReturnPiece.PieceType promotedType(boolean white, char promotionPiece) {
        char piece = Character.toUpperCase(promotionPiece);
        return switch (piece) {
            case 'Q' -> white ? ReturnPiece.PieceType.WQ : ReturnPiece.PieceType.BQ;
            case 'R' -> white ? ReturnPiece.PieceType.WR : ReturnPiece.PieceType.BR;
            case 'B' -> white ? ReturnPiece.PieceType.WB : ReturnPiece.PieceType.BB;
            case 'N' -> white ? ReturnPiece.PieceType.WN : ReturnPiece.PieceType.BN;
            default -> null;
        };
    }

    private static void updatePieceLocation(ReturnPiece piece, char file, char rank) {
        //update stored locatoin for the piece
        piece.pieceFile = ReturnPiece.PieceFile.valueOf(String.valueOf(file));
        piece.pieceRank = Character.getNumericValue(rank);
    }

    private static Player ownerOf(ReturnPiece piece) {
        return piece.pieceType.toString().charAt(0) == 'W' ? Player.white : Player.black;
    }

    private static Player opposite(Player player) {
        return player == Player.white ? Player.black : Player.white;
    }

    private static boolean isPawn(ReturnPiece piece) {
        return piece.pieceType == ReturnPiece.PieceType.WP || piece.pieceType == ReturnPiece.PieceType.BP;
    }

    private static boolean isKing(ReturnPiece piece) {
        return piece.pieceType == ReturnPiece.PieceType.WK || piece.pieceType == ReturnPiece.PieceType.BK;
    }

    private static boolean isRook(ReturnPiece piece) {
        return piece.pieceType == ReturnPiece.PieceType.WR || piece.pieceType == ReturnPiece.PieceType.BR;
    }

    private static boolean isCastlingTarget(
            int srcRow,
            int srcCol,
            int dstRow,
            int dstCol,
            Player player) {

        int homeRow = player == Player.white ? 7 : 0;
        return srcRow == homeRow
                && dstRow == homeRow
                && srcCol == 4
                && (dstCol == 6 || dstCol == 2);
    }
}
