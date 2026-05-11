package org.example.sudobeats.dto.response;

import org.example.sudobeats.entity.enums.Difficulty;

import java.util.List;
import java.util.UUID;

/**
 * Music track payload for GET /api/games/{id}/track.
 *
 * The {@code moves} list contains only the CORRECT moves, sorted by
 * {@code timestampFromStart} ascending.  The front-end maps each move
 * to a musical note (e.g. value 1-9 → scale degree) and plays them
 * back in timestamp order to reconstruct the melody the player "composed"
 * while solving the puzzle.
 */
public record TrackResponse(
        UUID gameId,
        Difficulty difficulty,
        int totalCorrectMoves,
        long totalDurationMs,   // timestamp of the last correct move — melody length
        List<MoveResponse> moves
) {}
