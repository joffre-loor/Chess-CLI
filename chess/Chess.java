package chess;


import java.util.ArrayList;
//import java.util.List; 

public class Chess {

        enum Player { white, black }

		// game state fields 
		private static ReturnPiece[][] board;
		private static Player curTurn;
		
    
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

		/* FILL IN THIS METHOD */
		ReturnPlay result = new ReturnPlay();
		// just testing if it moves 
		String[] moveSplit = move.split(" ");

		//parse 
		char srcFile = moveSplit[0].charAt(0);
		int srcRank = moveSplit[0].charAt(1);
		char desFile = moveSplit[1].charAt(0);
		char desRank = moveSplit[1].charAt(1);

		int srcRow = 8 - (srcRank - '0');
		int srcCol = srcFile - 'a';
		int desRow = 8 - (desRank - '0');
		int desCol = desFile - 'a';

		ReturnPiece piece = board[srcRow][srcCol];

		// check if there is a piece at source, illegal move check 
		if (piece == null){
			result.piecesOnBoard = getPieces(); 
			result.message = ReturnPlay.Message.ILLEGAL_MOVE; 
			return result; 
		}	

		// check if the move is legal for the piece type, illegal move check
		if (!MoveRules.isLegal(board, piece, srcRow, srcCol, desRow, desCol)) {
			result.piecesOnBoard = getPieces(); 
			result.message = ReturnPlay.Message.ILLEGAL_MOVE; 
			return result; 
		}


		// avoid taking your own piece FILL IN!!
		
		// create turn order FILL IN!!

		// switching turns FILL IN!!

		if (piece != null) {
			board[desRow][desCol] = piece;
			board[srcRow][srcCol] = null;

			//update stored locatoin for the piece
			piece.pieceFile = ReturnPiece.PieceFile.valueOf(String.valueOf(desFile));
			piece.pieceRank = Character.getNumericValue(desRank);

		}

		// FILL IN 

		result.piecesOnBoard = getPieces();
		result.message = null;  //filler for now
		return result; 
		
		/* FOLLOWING LINE IS A PLACEHOLDER TO MAKE COMPILER HAPPY */
		/* WHEN YOU FILL IN THIS METHOD, YOU NEED TO RETURN A ReturnPlay OBJECT */
	}
	
	// helper method to make pieces for start() method
	private static ReturnPiece createPiece(ReturnPiece.PieceType type, char file, int rank) {
		ReturnPiece p = new ReturnPiece();
		p.pieceType = type;
		p.pieceFile = ReturnPiece.PieceFile.valueOf(String.valueOf(file));
		p.pieceRank = rank;
		return p;
	}

	private static ArrayList<ReturnPiece> getPieces() {
		ArrayList<ReturnPiece> pieces = new ArrayList<>();
		for (int r=0; r < 8; r++) {
			for (int c=0; c < 8; c++) {
				if (board[r][c] != null) {
					pieces.add(board[r][c]);
				}
			}
		}
		return pieces;
	}

	/**
	 * This method should reset the game, and start from scratch.
	 */
	public static void start() {
		/* FILL IN THIS METHOD */
		board = new ReturnPiece[8][8];
		curTurn = Player.white; 

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
		for (int c=0; c <8; c++){
			board[1][c] = createPiece(ReturnPiece.PieceType.BP, (char)('a'+c), 7);
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
		for (int c=0; c <8; c++){
			board[6][c] = createPiece(ReturnPiece.PieceType.WP, (char)('a'+c), 2);
		}


	}
}
