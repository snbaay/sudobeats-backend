package org.example.sudobeats.service;

import org.example.sudobeats.dto.GameplayDtos.HintResponse;
import org.springframework.stereotype.Service;

@Service
public class

CoachService {
    public HintResponse explainHint() {
        return new HintResponse(
                4,
                4,
                5,
                "Only candidate remains",
                "The row, column, and 3x3 box already block every other digit, so 5 is the only legal placement.",
                "Single candidate");
    }
}

