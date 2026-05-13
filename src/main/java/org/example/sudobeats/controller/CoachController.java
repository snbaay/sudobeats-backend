package org.example.sudobeats.controller;

import org.example.sudobeats.service.CoachService;
import org.example.sudobeats.dto.GameplayDtos.*;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
public class CoachController {
    private final CoachService coachService;

    public CoachController(CoachService coachService) { this.coachService = coachService; }

    @PostMapping("/{gameId}/hint")
    public HintResponse hint(@PathVariable UUID gameId, @RequestBody HintRequest request) {
        return coachService.explainHint();
    }
}

