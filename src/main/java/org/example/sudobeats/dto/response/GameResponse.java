package org.example.sudobeats.dto.response;


import org.example.sudobeats.entity.enums.Difficulty;
import org.example.sudobeats.entity.enums.GameStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Safe projection of SudokuGame.
 *
 * SECURITY: solutionBoard is intentionally excluded — exposing it would let
 * the client trivially cheat.  Only initialBoard + currentBoard are returned.
 */
public record GameResponse(
        UUID id,
        UUID userId,
        String username,
        int[][] initialBoard,
        int[][] currentBoard,
        Difficulty difficulty,
        GameStatus status,
        Instant startedAt,
        Instant completedAt,
        long totalMoves,
        long correctMoves
) {}
