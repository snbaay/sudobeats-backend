package org.example.sudobeats.service;

import org.example.sudobeats.entity.SharedTrack;
import org.example.sudobeats.repository.SharedTrackRepository;
import org.example.sudobeats.dto.GameplayDtos.*;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrackService {
    private final SharedTrackRepository tracks;

    public TrackService(SharedTrackRepository tracks) { this.tracks = tracks; }

    @Transactional
    public ShareTrackResponse create(ShareTrackRequest request) {
        SharedTrack track = new SharedTrack();
        track.setTitle(request.title());
        track.setSoundpack(request.soundpack());
        track.setMovesJson("[]");
        tracks.save(track);
        return new ShareTrackResponse(track.getPublicSlug());
    }

    public Map<String, Object> find(UUID slug) {
        SharedTrack track = tracks.findByPublicSlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Track not found"));
        return Map.of(
                "id", track.getPublicSlug(),
                "title", "Shared SudoBeats Track",
                "username", "player",
                "difficulty", "MEDIUM",
                "completionTimeSeconds", 0,
                "mistakes", 0,
                "soundpack", "synthwave",
                "moves", java.util.List.of());
    }
}

