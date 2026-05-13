package org.example.sudobeats.dto;


import java.util.UUID;

public final class GameplayDtos {
    private GameplayDtos() {}

    public record HintRequest(Integer row, Integer col) {}
    public record HintResponse(int row, int col, int value, String title, String explanation, String strategy) {}
    public record ShareTrackRequest(UUID gameId, String soundpack, String title) {}
    public record ShareTrackResponse(UUID shareId) {}
    public record LeaderboardRow(int rank, String username, org.example.sudobeats.entity.enums.Difficulty difficulty, int completionTimeSeconds, int mistakes, String completedAt) {}
}

