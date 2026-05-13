package org.example.sudobeats.dto;

import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(String username, String email, String password) {}
    public record LoginRequest(String email, String password) {}
    public record UserView(UUID id, String username, String email, int currentStreak, boolean pro) {}
    public record AuthResponse(String accessToken, UserView user) {}
}
