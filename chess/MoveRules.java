package chess;

public class MoveRules {

    // this class is to put all the different pieces/rules in one place
    // pawn, rook, knight, bishop, queen, king
    static boolean isLegal(
            ReturnPiece[][] board,
            ReturnPiece piece,
            int srcRow,
            int srcCol,
            int dstRow,
            int dstCol) {

        // check if the move is legal for the piece type
        if (srcRow == dstRow && srcCol == dstCol) {
            return false;
        }

        return switch (piece.pieceType) {
            case WP, BP -> isLegalPawnMove(board, piece, srcRow, srcCol, dstRow, dstCol);
            case WR, BR -> isLegalRookMove(board, srcRow, srcCol, dstRow, dstCol);
            case WN, BN -> isLegalKnightMove(srcRow, srcCol, dstRow, dstCol);
            case WB, BB -> isLegalBishopMove(board, srcRow, srcCol, dstRow, dstCol);
            case WQ, BQ -> isLegalQueenMove(board, srcRow, srcCol, dstRow, dstCol);
            case WK, BK -> isLegalKingMove(srcRow, srcCol, dstRow, dstCol);
        };
    }

    private static boolean isLegalPawnMove(
            ReturnPiece[][] board,
            ReturnPiece piece,
            int srcRow,
            int srcCol,
            int dstRow,
            int dstCol) {

        // check if the move is legal for a pawn
        // white pawns move up the board (decreasing row number), black pawns move down the board (increasing row number)
        // can move forward 1, 2 on first move, and diagonal capture
        boolean white = isWhite(piece);
        int direction = white ? -1 : 1;
        int startRow = white ? 6 : 1;
        int rowDiff = dstRow - srcRow;
        int colDiff = dstCol - srcCol;

        if (colDiff == 0) {
            if (rowDiff == direction) {
                // can move 1 square forward if destination is empty
                return board[dstRow][dstCol] == null;
            }
            if (srcRow == startRow && rowDiff == 2 * direction) {
                // first move can move 2 squares forward if both are empty
                return board[srcRow + direction][srcCol] == null && board[dstRow][dstCol] == null;
            }
            return false;
        }

        if (Math.abs(colDiff) == 1 && rowDiff == direction) {
            // can capture diagonally
            ReturnPiece target = board[dstRow][dstCol];
            return target != null && isWhite(target) != white;
        }

        return false;
    }

    private static boolean isLegalRookMove(
            ReturnPiece[][] board,
            int srcRow,
            int srcCol,
            int dstRow,
            int dstCol) {

        // check if the move is legal for a rook
        // can move either direction just not diagonal, cannot leap over other pieces
        if (srcRow != dstRow && srcCol != dstCol) {
            // not moving along the same row or column
            return false;
        }
        return pathClear(board, srcRow, srcCol, dstRow, dstCol);
    }

    private static boolean isLegalKnightMove(int srcRow, int srcCol, int dstRow, int dstCol) {
        // check if the move is legal for a knight
        // knight can move in L shape (2 one way then 1 other way), and can leap over pieces
        int rowDiff = Math.abs(dstRow - srcRow);
        int colDiff = Math.abs(dstCol - srcCol);
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    private static boolean isLegalBishopMove(
            ReturnPiece[][] board,
            int srcRow,
            int srcCol,
            int dstRow,
            int dstCol) {

        // check if the move is legal for a bishop
        // can move diagonally any number of squares, cannot leap over pieces
        int rowDiff = Math.abs(dstRow - srcRow);
        int colDiff = Math.abs(dstCol - srcCol);
        if (rowDiff != colDiff) {
            return false;
        }
        return pathClear(board, srcRow, srcCol, dstRow, dstCol);
    }

    private static boolean isLegalQueenMove(
            ReturnPiece[][] board,
            int srcRow,
            int srcCol,
            int dstRow,
            int dstCol) {

        // check if the move is legal for a queen
        // can move in any direction any number of squares, cannot leap over pieces
        // can use rook and bishop bc queen is combo of the two
        return isLegalRookMove(board, srcRow, srcCol, dstRow, dstCol)
                || isLegalBishopMove(board, srcRow, srcCol, dstRow, dstCol);
    }

    private static boolean isLegalKingMove(int srcRow, int srcCol, int dstRow, int dstCol) {
        // check if the move is legal for a king
        // can move 1 square in any direction
        int rowDiff = Math.abs(dstRow - srcRow);
        int colDiff = Math.abs(dstCol - srcCol);
        return rowDiff <= 1 && colDiff <= 1 && (rowDiff != 0 || colDiff != 0);
    }

    private static boolean pathClear(
            ReturnPiece[][] board,
            int srcRow,
            int srcCol,
            int dstRow,
            int dstCol) {

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

    private static boolean isWhite(ReturnPiece piece) {
        return piece.pieceType.toString().charAt(0) == 'W';
    }
}
