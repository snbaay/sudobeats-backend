package org.example.sudobeats.engine;

import org.example.sudobeats.entity.enums.Difficulty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Pure Sudoku logic — stateless and thread-safe (no mutable fields).
 *
 * <h3>Board generation algorithm</h3>
 * <ol>
 *   <li>Fill the three independent diagonal 3×3 boxes with shuffled 1–9
 *       (they can't conflict with each other, so no backtracking needed).</li>
 *   <li>Use recursive backtracking to fill the remaining 54 cells, trying
 *       candidates in a shuffled order so every call produces a unique board.</li>
 *   <li>Clone the completed solution and remove N cells according to difficulty
 *       to produce the puzzle the player will solve.</li>
 * </ol>
 *
 * <h3>Move validation algorithm</h3>
 * <ol>
 *   <li>Reject if the target cell is a clue (initialBoard[row][col] != 0).</li>
 *   <li>Reject if placing {@code value} at {@code (row, col)} would violate
 *       the row-, column-, or 3×3-box uniqueness constraint.</li>
 * </ol>
 */
@Component
public class SudokuEngine {

    private static final int SIZE     = 9;
    private static final int BOX_SIZE = 3;

    // ── Board Generation ──────────────────────────────────────────────────────

    /**
     * Generates a complete, valid 9×9 Sudoku solution.
     * Each call returns a statistically unique board thanks to shuffle randomisation.
     */
    public int[][] generateSolution() {
        int[][] board = new int[SIZE][SIZE];
        Random random = new Random();

        // Step 1: fill the three diagonal boxes independently
        fillDiagonalBoxes(board, random);

        // Step 2: solve the rest with backtracking
        if (!solveBoard(board, random)) {
            // Should never happen after filling the diagonal; handle defensively
            throw new IllegalStateException("Sudoku engine failed to generate a valid solution");
        }

        return board;
    }

    /**
     * Creates a playable puzzle from a solved board by blanking N cells.
     *
     * @param solution  a fully solved 9×9 board (will NOT be modified)
     * @param difficulty controls how many cells are removed
     * @return a new board where 0 represents an empty cell
     */
    public int[][] generatePuzzle(int[][] solution, Difficulty difficulty) {
        int[][] puzzle = deepCopy(solution);
        int cellsToRemove = switch (difficulty) {
            case EASY   -> 30;   // ~33% empty — plenty of clues
            case MEDIUM -> 45;   // ~56% empty — requires logical deduction
            case HARD   -> 55;   // ~68% empty — sparse, demanding
        };
        removeCells(puzzle, cellsToRemove);
        return puzzle;
    }

    // ── Move Validation ───────────────────────────────────────────────────────

    /**
     * Returns {@code true} if placing {@code value} at {@code (row, col)}
     * in {@code currentBoard} is a legal Sudoku move.
     *
     * <p>Checks:
     * <ul>
     *   <li>No other cell in the same row contains {@code value}.</li>
     *   <li>No other cell in the same column contains {@code value}.</li>
     *   <li>No other cell in the same 3×3 box contains {@code value}.</li>
     * </ul>
     * The target cell is treated as empty during the check (its previous content
     * is temporarily ignored), so overwriting an incorrect guess is always valid
     * provided the new value doesn't violate the rules elsewhere.
     */
    public boolean isValidPlacement(int[][] currentBoard, int row, int col, int value) {
        // Temporarily blank the cell so its old content doesn't cause false positives
        int previous = currentBoard[row][col];
        currentBoard[row][col] = 0;
        boolean valid = !hasConflict(currentBoard, row, col, value);
        currentBoard[row][col] = previous;
        return valid;
    }

    /**
     * Returns {@code true} if {@code current[row][col] == solution[row][col]}
     * for the given placement — i.e. the move is "musically correct".
     */
    public boolean isCorrectPlacement(int[][] solution, int row, int col, int value) {
        return solution[row][col] == value;
    }

    /**
     * Returns {@code true} when every cell of {@code current} matches {@code solution}.
     */
    public boolean isBoardComplete(int[][] current, int[][] solution) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (current[i][j] != solution[i][j]) return false;
            }
        }
        return true;
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /** Fills the top-left, center, and bottom-right 3×3 boxes randomly. */
    private void fillDiagonalBoxes(int[][] board, Random random) {
        for (int box = 0; box < SIZE; box += BOX_SIZE) {
            fillBox(board, box, box, random);
        }
    }

    /** Fills one 3×3 box starting at (startRow, startCol) with a shuffled 1–9. */
    private void fillBox(int[][] board, int startRow, int startCol, Random random) {
        List<Integer> nums = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
        Collections.shuffle(nums, random);
        int idx = 0;
        for (int i = 0; i < BOX_SIZE; i++) {
            for (int j = 0; j < BOX_SIZE; j++) {
                board[startRow + i][startCol + j] = nums.get(idx++);
            }
        }
    }

    /**
     * Recursive backtracking solver.
     * Scans left-to-right, top-to-bottom for the first empty cell,
     * tries each valid digit in shuffled order, and recurses.
     * Returns {@code true} when the board is fully filled.
     */
    private boolean solveBoard(int[][] board, Random random) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) {
                    List<Integer> candidates = getShuffledCandidates(board, row, col, random);
                    for (int candidate : candidates) {
                        board[row][col] = candidate;
                        if (solveBoard(board, random)) return true;
                        board[row][col] = 0;  // backtrack
                    }
                    return false;  // no candidate worked — need to backtrack further
                }
            }
        }
        return true;  // no empty cells remain — board is complete
    }

    /** Builds a list of digits 1–9 that don't conflict at (row, col), in random order. */
    private List<Integer> getShuffledCandidates(int[][] board, int row, int col, Random random) {
        List<Integer> candidates = new ArrayList<>();
        for (int num = 1; num <= SIZE; num++) {
            if (!hasConflict(board, row, col, num)) {
                candidates.add(num);
            }
        }
        Collections.shuffle(candidates, random);
        return candidates;
    }

    /**
     * Returns {@code true} if placing {@code num} at {@code (row, col)}
     * would violate the Sudoku row, column, or box uniqueness constraint.
     * Assumes board[row][col] is 0 (the cell is being evaluated as empty).
     */
    private boolean hasConflict(int[][] board, int row, int col, int num) {
        // Row check
        for (int j = 0; j < SIZE; j++) {
            if (board[row][j] == num) return true;
        }
        // Column check
        for (int i = 0; i < SIZE; i++) {
            if (board[i][col] == num) return true;
        }
        // 3×3 box check
        int boxRow = (row / BOX_SIZE) * BOX_SIZE;
        int boxCol = (col / BOX_SIZE) * BOX_SIZE;
        for (int i = 0; i < BOX_SIZE; i++) {
            for (int j = 0; j < BOX_SIZE; j++) {
                if (board[boxRow + i][boxCol + j] == num) return true;
            }
        }
        return false;
    }

    /** Randomly blanks {@code count} cells in-place. */
    private void removeCells(int[][] board, int count) {
        // Build a shuffled list of all 81 positions
        List<int[]> positions = new ArrayList<>(SIZE * SIZE);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                positions.add(new int[]{i, j});
            }
        }
        Collections.shuffle(positions);

        int removed = 0;
        for (int[] pos : positions) {
            if (removed >= count) break;
            board[pos[0]][pos[1]] = 0;
            removed++;
        }
    }

    /** Deep-copies a 9×9 board so mutations don't affect the original. */
    public int[][] deepCopy(int[][] board) {
        int[][] copy = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, SIZE);
        }
        return copy;
    }
}
