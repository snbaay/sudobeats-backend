package org.example.sudobeats.engine;


import org.example.sudobeats.engine.SudokuEngine;
import org.example.sudobeats.entity.enums.Difficulty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SudokuEngineTest {

    private SudokuEngine engine;

    @BeforeEach
    void setUp() {
        engine = new SudokuEngine();
    }

    @Test
    @DisplayName("generateSolution() produces a complete 9x9 board")
    void solutionIsComplete() {
        int[][] board = engine.generateSolution();
        assertEquals(9, board.length);
        for (int[] row : board) {
            assertEquals(9, row.length);
            for (int val : row) {
                assertTrue(val >= 1 && val <= 9, "Every cell must be 1–9, got " + val);
            }
        }
    }

    @Test
    @DisplayName("generateSolution() rows have unique digits 1–9")
    void solutionRowsAreValid() {
        int[][] board = engine.generateSolution();
        for (int r = 0; r < 9; r++) {
            boolean[] seen = new boolean[10];
            for (int c = 0; c < 9; c++) {
                int v = board[r][c];
                assertFalse(seen[v], "Duplicate " + v + " in row " + r);
                seen[v] = true;
            }
        }
    }

    @Test
    @DisplayName("generateSolution() columns have unique digits 1–9")
    void solutionColumnsAreValid() {
        int[][] board = engine.generateSolution();
        for (int c = 0; c < 9; c++) {
            boolean[] seen = new boolean[10];
            for (int r = 0; r < 9; r++) {
                int v = board[r][c];
                assertFalse(seen[v], "Duplicate " + v + " in column " + c);
                seen[v] = true;
            }
        }
    }

    @Test
    @DisplayName("generateSolution() 3x3 boxes have unique digits 1–9")
    void solutionBoxesAreValid() {
        int[][] board = engine.generateSolution();
        for (int boxRow = 0; boxRow < 9; boxRow += 3) {
            for (int boxCol = 0; boxCol < 9; boxCol += 3) {
                boolean[] seen = new boolean[10];
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        int v = board[boxRow + i][boxCol + j];
                        assertFalse(seen[v], "Duplicate " + v + " in box (" + boxRow + "," + boxCol + ")");
                        seen[v] = true;
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("generatePuzzle(EASY) removes exactly 30 cells")
    void easyPuzzleHasCorrectEmptyCells() {
        int[][] solution = engine.generateSolution();
        int[][] puzzle   = engine.generatePuzzle(solution, Difficulty.EASY);
        long empty = countEmpty(puzzle);
        assertEquals(30, empty, "EASY should have 30 empty cells");
    }

    @Test
    @DisplayName("generatePuzzle(HARD) removes exactly 55 cells")
    void hardPuzzleHasCorrectEmptyCells() {
        int[][] solution = engine.generateSolution();
        int[][] puzzle   = engine.generatePuzzle(solution, Difficulty.HARD);
        assertEquals(55, countEmpty(puzzle));
    }

    @Test
    @DisplayName("isValidPlacement detects row conflict")
    void detectsRowConflict() {
        int[][] board = new int[9][9];
        board[0][0] = 5;
        // placing 5 anywhere else in row 0 should be invalid
        assertFalse(engine.isValidPlacement(board, 0, 4, 5));
    }

    @Test
    @DisplayName("isValidPlacement detects column conflict")
    void detectsColConflict() {
        int[][] board = new int[9][9];
        board[0][0] = 7;
        assertFalse(engine.isValidPlacement(board, 5, 0, 7));
    }

    @Test
    @DisplayName("isValidPlacement detects box conflict")
    void detectsBoxConflict() {
        int[][] board = new int[9][9];
        board[0][0] = 3;
        assertFalse(engine.isValidPlacement(board, 1, 1, 3));
    }

    @Test
    @DisplayName("isValidPlacement allows a non-conflicting value")
    void allowsValidPlacement() {
        int[][] board = new int[9][9];
        board[0][0] = 1;
        assertTrue(engine.isValidPlacement(board, 4, 4, 9));
    }

    @Test
    @DisplayName("isBoardComplete returns true only when boards match exactly")
    void boardCompletion() {
        int[][] solution = engine.generateSolution();
        int[][] copy     = engine.deepCopy(solution);
        assertTrue(engine.isBoardComplete(copy, solution));
        copy[0][0] = (copy[0][0] % 9) + 1; // mutate one cell
        assertFalse(engine.isBoardComplete(copy, solution));
    }

    private long countEmpty(int[][] board) {
        long count = 0;
        for (int[] row : board) for (int v : row) if (v == 0) count++;
        return count;
    }
}
