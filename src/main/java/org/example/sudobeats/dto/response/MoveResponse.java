package org.example.sudobeats.dto.response;


import java.util.UUID;

/**
 * Represents one recorded player action.
 * {@code correct} tells the client whether this move matches the solution
 * so the UI can give instant feedback without exposing the full solution board.
 */
public record MoveResponse(
        UUID id,
        UUID gameId,
        int row,
        int col,
        int value,
        long timestampFromStart,
        boolean correct
) {}
