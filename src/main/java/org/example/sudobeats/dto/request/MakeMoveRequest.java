package org.example.sudobeats.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Payload for POST /api/games/{id}/moves
 */
public record MakeMoveRequest(

        @NotNull(message = "row is required")
        @Min(value = 0, message = "row must be between 0 and 8")
        @Max(value = 8, message = "row must be between 0 and 8")
        Integer row,

        @NotNull(message = "col is required")
        @Min(value = 0, message = "col must be between 0 and 8")
        @Max(value = 8, message = "col must be between 0 and 8")
        Integer col,

        @NotNull(message = "value is required")
        @Min(value = 1, message = "value must be between 1 and 9")
        @Max(value = 9, message = "value must be between 1 and 9")
        Integer value
) {}
