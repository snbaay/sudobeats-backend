package org.example.sudobeats.entity;


import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shared_tracks")
public class SharedTrack {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private SudokuGame game;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "public_slug", nullable = false, unique = true)
    private UUID publicSlug = UUID.randomUUID();

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 32)
    private String soundpack;

    @Column(name = "moves_json", nullable = false, columnDefinition = "jsonb")
    private String movesJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getPublicSlug() { return publicSlug; }
    public void setGame(SudokuGame game) { this.game = game; }
    public void setOwner(User owner) { this.owner = owner; }
    public void setTitle(String title) { this.title = title; }
    public void setSoundpack(String soundpack) { this.soundpack = soundpack; }
    public void setMovesJson(String movesJson) { this.movesJson = movesJson; }
}

