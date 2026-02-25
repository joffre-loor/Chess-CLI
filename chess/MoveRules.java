package chess;

public class MoveRules {
    
// this class is to put all the different pieces/rules in one place
// pawn, rook, knight, bishop, queen, king
    static boolean isLegal(ReturnPiece[][] board, ReturnPiece piece, int srcRow, int srcCol, int desRow, int desCol) {
        // check if the move is legal for the piece type
        return switch (piece.pieceType) {
            case WP, BP -> isLegalPawnMove(board, piece, srcRow, srcCol, desRow, desCol);
            case WR, BR -> isLegalRookMove(board, piece, srcRow, srcCol, desRow, desCol);
            case WN, BN -> isLegalKnightMove(board, piece, srcRow, srcCol, desRow, desCol);
            case WB, BB -> isLegalBishopMove(board, piece, srcRow, srcCol, desRow, desCol);
            case WQ, BQ -> isLegalQueenMove(board, piece, srcRow, srcCol, desRow, desCol);
            case WK, BK -> isLegalKingMove(board, piece, srcRow, srcCol, desRow, desCol);        
        }; 
    }

    private static boolean isLegalPawnMove(ReturnPiece[][] board, ReturnPiece piece, int srcRow, int srcCol, int desRow, int desCol) {
        // check if the move is legal for a pawn
        // white pawns move up the board (decreasing row number), black pawns move down the board (increasing row number)
        // can move forward 1, 2 on first move, and diagonal capture 
        if (piece.pieceType == ReturnPiece.PieceType.WP) {
            // white pawn
            if (srcRow == 6 && desRow == 4 && srcCol == desCol) {
                // first move can move 2 squares forward
                return true;
            } else if (desRow == srcRow - 1 && srcCol == desCol) {
                // can move 1 square forward
                return true;
            } else if (desRow == srcRow - 1 && Math.abs(srcCol - desCol) == 1) {
                // can capture diagonally
                return true;
            }
        } else {
            // black pawn, same but just backwards 
            if (srcRow == 1 && desRow == 3 && srcCol == desCol) {
                // first move can move 2 squares forward
                return true;
            } else if (desRow == srcRow + 1 && srcCol == desCol) {
                // can move 1 square forward
                return true;
            } else if (desRow == srcRow + 1 && Math.abs(srcCol - desCol) == 1) {
                // can capture diagonally
                return true;
            }
        }
        return false;
    }


    private static boolean isLegalRookMove(ReturnPiece[][] board, ReturnPiece piece, int srcRow, int srcCol, int desRow, int desCol) {
        // check if the move is legal for a rook
        // can move either direction just not diagonal, cannot leap over other pieces
        if (srcRow == desRow) {
            // moving along the same row
            int step = srcCol < desCol ? 1 : -1;
            for (int c = srcCol + step; c != desCol; c += step) {
                if (board[srcRow][c] != null) {
                    return false; // there is a piece in the way
                }
            }
            return true;
        } else if (srcCol == desCol) {
            // moving along the same column
            int step = srcRow < desRow ? 1 : -1;
            for (int r = srcRow + step; r != desRow; r += step) {
                if (board[r][srcCol] != null) {
                    return false; // there is a piece in the way
                }
            }
            return true;
        }
        return false; // not moving along the same row or column
    }

    private static boolean isLegalKnightMove(ReturnPiece[][] board, ReturnPiece piece, int srcRow, int srcCol, int desRow, int desCol){
        // check if the move is legal for a knight
        // knight can move in L shape (2 one way then 1 other way), and can leap over pieces
        int rowDiff = Math.abs(desRow - srcRow);
        int colDiff = Math.abs(desCol - srcCol);
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    private static boolean isLegalBishopMove(ReturnPiece[][] board, ReturnPiece piece, int srcRow, int srcCol, int desRow, int desCol) {
        // check if the move is legal for a bishop
        // can move diagonally any number of squares, cannot leap over pieces
        if (Math.abs(desRow - srcRow) == Math.abs(desCol - srcCol)) {
            int rowStep = desRow > srcRow ? 1 : -1;
            int colStep = desCol > srcCol ? 1 : -1;
            int r = srcRow + rowStep;
            int c = srcCol + colStep;
            while (r != desRow && c != desCol) {
                if (board[r][c] != null) {
                    return false; // there is a piece in the way
                }
                r += rowStep;
                c += colStep;
            }
            return true;
        }
        return false; 
    }

    private static boolean isLegalQueenMove(ReturnPiece[][] board, ReturnPiece piece, int srcRow, int srcCol, int desRow, int desCol) {
        // check if the move is legal for a queen
        // can move in any direction any number of squares, cannot leap over pieces
        // can use rook and bishop bc queen is combo of the two 
        return isLegalRookMove(board, piece, srcRow, srcCol, desRow, desCol) ||
               isLegalBishopMove(board, piece, srcRow, srcCol, desRow, desCol);
    }

    private static boolean isLegalKingMove(ReturnPiece[][] board, ReturnPiece piece, int srcRow, int srcCol, int desRow, int desCol) {
        // check if the move is legal for a king
        // can move 1 square in any direction, cannot leap over pieces
        int rowDiff = Math.abs(desRow - srcRow);
        int colDiff = Math.abs(desCol - srcCol);
        return rowDiff <= 1 && colDiff <= 1;
}
} 