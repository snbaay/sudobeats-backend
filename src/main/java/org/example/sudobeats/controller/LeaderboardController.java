package org.example.sudobeats.controller;

import org.example.sudobeats.service.LeaderboardService;
import org.example.sudobeats.dto.GameplayDtos.LeaderboardRow;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {
    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public List<LeaderboardRow> list(@RequestParam(defaultValue = "MEDIUM") String difficulty) {
        return leaderboardService.list(difficulty);
    }
}

