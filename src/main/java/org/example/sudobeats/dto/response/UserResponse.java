package org.example.sudobeats.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * The only User shape the client ever sees.
 * Deliberately omits the full games list to keep responses lean.
 */
public record UserResponse(
        UUID id,
        String username,
        String email,
        Instant createdAt
) {}
