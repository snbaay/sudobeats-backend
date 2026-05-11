package org.example.sudobeats.entity.enums;

/**
 * Controls how many cells are removed from the solved board when generating a puzzle.
 * EASY   → 30 cells removed  (~33% empty)
 * MEDIUM → 45 cells removed  (~56% empty)
 * HARD   → 55 cells removed  (~68% empty)
 */
public enum Difficulty {
    EASY,
    MEDIUM,
    HARD
}
