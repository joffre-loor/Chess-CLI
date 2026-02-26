# Chess-CLI

Terminal-based two-player chess implementation in Java.

## Overview

This project implements chess game state and move execution around:

- `chess.Chess.start()` to initialize/reset the board
- `chess.Chess.play(String move)` to apply one move and return a `ReturnPlay` result

The core logic returns structured game state (`piecesOnBoard` + `message`) instead of relying on printed output.

## Implemented Rules

- Standard piece movement (pawn, rook, knight, bishop, queen, king)
- Turn order enforcement
- Illegal move handling (move not applied)
- Self-check prevention (cannot make a move that leaves own king in check)
- Check detection
- Checkmate detection
- Castling (kingside and queenside, with legal constraints)
- En passant
- Pawn promotion
  - Explicit piece: `N`, `R`, `B`, `Q`
  - Default promotion to queen when omitted
- Draw offers via `draw?` suffix (move executes first)
- Resign via `resign`
- Full game reset via `Chess.start()`

## Project Structure

```text
chess/
  Chess.java
  MoveRules.java
  PlayChess.java          (local testing helper)
  ReturnPiece.java
  ReturnPlay.java
README.md
```

## Local Run

From the repository root:

```powershell
javac chess\*.java
java chess.PlayChess
```

Example moves:

```text
e2 e4
e7 e5
g1 f3
```

Special formats:

- Castling: `e1 g1` or `e1 c1`
- Promotion: `g7 g8 N` (or `g7 g8` for default queen)
- Draw offer: `g1 f3 draw?`
- Resign: `resign`

## Notes

- `Chess.start()` resets the board to the initial position.
- `Chess.play(String move)` applies a single move and returns the resulting game state.
- Compiled `.class` files are build artifacts and should not be committed.
