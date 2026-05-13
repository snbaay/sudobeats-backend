package org.example.sudobeats.controller;

import org.example.sudobeats.service.TrackService;
import org.example.sudobeats.dto.GameplayDtos.*;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracks")
public class TrackController {
    private final TrackService trackService;

    public TrackController(TrackService trackService) { this.trackService = trackService; }

    @PostMapping
    public ShareTrackResponse create(@RequestBody ShareTrackRequest request) {
        return trackService.create(request);
    }

    @GetMapping("/{slug}")
    public Map<String, Object> get(@PathVariable UUID slug) {
        return trackService.find(slug);
    }
}

