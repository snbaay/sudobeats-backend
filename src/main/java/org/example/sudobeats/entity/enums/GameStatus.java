package org.example.sudobeats.entity.enums;

/**
 * Lifecycle states of a Sudoku game session.
 */
public enum GameStatus {
    /** The player is actively solving the puzzle. */
    IN_PROGRESS,

    /** Every cell matches the solution — the player won. */
    COMPLETED,

    /** The player explicitly quit the game. */
    ABANDONED
}
