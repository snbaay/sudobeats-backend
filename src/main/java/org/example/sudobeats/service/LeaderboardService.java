package org.example.sudobeats.service;


import org.example.sudobeats.repository.SudokuGameRepository;
import org.example.sudobeats.dto.GameplayDtos.LeaderboardRow;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class LeaderboardService {
    private final SudokuGameRepository
            games;

    public LeaderboardService(SudokuGameRepository
                                      games) { this.games = games; }

    public List<LeaderboardRow> list(String difficulty) {
        AtomicInteger rank = new AtomicInteger(1);
        return games.leaderboard(difficulty).stream()
                .limit(100)
                .map(game -> new LeaderboardRow(
                        rank.getAndIncrement(),
                        game.getUser().getUsername(),
                        game.getDifficulty(),
                        0,
                        game.getMistakes(),
                        ""))
                .toList();
    }
}

