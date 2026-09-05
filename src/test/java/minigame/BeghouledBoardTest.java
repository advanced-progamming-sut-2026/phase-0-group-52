package minigame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeghouledBoardTest {

    @Test
    void aFreshBoardIsFullAndUnscored() {
        for (int attempt = 0; attempt < 20; attempt++) {
            BeghouledBoard board = new BeghouledBoard(1);
            for (int c = 0; c < BeghouledBoard.COLUMNS; c++) {
                for (int r = 0; r < BeghouledBoard.ROWS; r++) {
                    assertNotNull(board.at(c, r), "every cell starts filled");
                }
            }
            assertEquals(0, board.getSun(), "a fresh board has not scored yet");
        }
    }

    @Test
    void onlyNeighboursCanSwap() {
        BeghouledBoard board = new BeghouledBoard(1);
        assertFalse(board.swap(0, 0, 4, 4), "distant cells must not swap");
        assertFalse(board.swap(0, 0, 0, 0), "a cell cannot swap with itself");
        assertTrue(BeghouledBoard.areNeighbours(1, 1, 1, 2), "vertical neighbours touch");
        assertTrue(BeghouledBoard.areNeighbours(1, 1, 2, 1), "horizontal neighbours touch");
    }

    @Test
    void aSwapThatMatchesNothingIsRefusedAndCostsNoMove() {
        BeghouledBoard board = new BeghouledBoard(1);
        int moves = board.getMovesLeft();
        for (int c = 0; c < BeghouledBoard.COLUMNS - 1; c++) {
            for (int r = 0; r < BeghouledBoard.ROWS; r++) {
                if (!board.wouldMatch(c, r, c + 1, r)) {
                    assertFalse(board.swap(c, r, c + 1, r), "a dead swap is refused");
                    assertEquals(moves, board.getMovesLeft(), "and costs nothing");
                    return;
                }
            }
        }
    }

    @Test
    void aMatchingSwapScoresAndSpendsExactlyOneMove() {
        for (int attempt = 0; attempt < 40; attempt++) {
            BeghouledBoard board = new BeghouledBoard(1);
            int moves = board.getMovesLeft();
            for (int c = 0; c < BeghouledBoard.COLUMNS - 1; c++) {
                for (int r = 0; r < BeghouledBoard.ROWS; r++) {
                    if (board.wouldMatch(c, r, c + 1, r)) {
                        assertTrue(board.swap(c, r, c + 1, r), "a live swap goes through");
                        assertEquals(moves - 1, board.getMovesLeft(), "one move spent");
                        assertTrue(board.getSun() > 0, "clearing tiles pays sun");
                        assertFalse(board.getLastCleared().isEmpty(),
                                "and reports what it cleared so the view can animate it");
                        return;
                    }
                }
            }
        }
    }

    @Test
    void theBoardNeverDeadEnds() {
        BeghouledBoard board = new BeghouledBoard(1);
        for (int turn = 0; turn < 60 && !board.isWon(); turn++) {
            assertTrue(board.hasAnyMove(), "there is always a legal swap available");
            boolean moved = false;
            for (int c = 0; c < BeghouledBoard.COLUMNS - 1 && !moved; c++) {
                for (int r = 0; r < BeghouledBoard.ROWS && !moved; r++) {
                    moved = board.swap(c, r, c + 1, r);
                }
            }
            if (!moved) {
                break;
            }
        }
    }

    @Test
    void aFreshBoardIsNeitherWonNorLost() {
        BeghouledBoard board = new BeghouledBoard(1);
        assertFalse(board.isLost(), "a fresh board is not lost");
        assertFalse(board.isWon(), "nor won");
        assertTrue(board.getTarget() > 0, "there is a sun target to chase");
    }
}
