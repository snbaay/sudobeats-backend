package org.example.sudobeats.dto.request;

import org.example.sudobeats.entity.enums.Difficulty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Payload for POST /api/games
 */
public record CreateGameRequest(

        @NotNull(message = "userId is required")
        UUID userId,

        @NotNull(message = "difficulty is required — choose EASY, MEDIUM, or HARD")
        Difficulty difficulty
) {}
