package org.example.sudobeats.entity;

import org.example.sudobeats.config.BoardConverter;
import org.example.sudobeats.entity.enums.Difficulty;
import org.example.sudobeats.entity.enums.GameStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A single Sudoku game session.
 *
 * Thread-safety notes:
 *  - @Version enables JPA optimistic locking: concurrent writes to the same row
 *    will throw OptimisticLockException instead of silently overwriting each other.
 *  - GameService also holds a per-game ReentrantLock for in-process serialisation.
 */
@Entity
@Table(name = "sudoku_games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SudokuGame {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Owning side of the User ↔ SudokuGame association. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The original clue cells (0 = empty cell, 1-9 = given clue).
     * Stored as a JSON string in a TEXT column via BoardConverter.
     * NEVER updated after game creation — used to prevent overwriting clues.
     */
    @Convert(converter = BoardConverter.class)
    @Column(name = "initial_board", nullable = false, columnDefinition = "TEXT")
    private int[][] initialBoard;

    /**
     * The player's live board state.  Only this column is mutated on each move.
     */
    @Convert(converter = BoardConverter.class)
    @Column(name = "current_board", nullable = false, columnDefinition = "TEXT")
    private int[][] currentBoard;

    /**
     * The complete correct solution.  Never exposed to the client.
     * Used for move correctness scoring (music track feature).
     */
    @Convert(converter = BoardConverter.class)
    @Column(name = "solution_board", nullable = false, columnDefinition = "TEXT")
    private int[][] solutionBoard;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private GameStatus status = GameStatus.IN_PROGRESS;

    /** Wall-clock time when the game was created — used to compute move timestamps. */
    @CreationTimestamp
    @Column(name = "started_at", updatable = false, nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
    @Column(name = "soundpack", nullable = false, length = 32)
    @Builder.Default
    private String soundpack = "synthwave";

    @Column(name = "mode", nullable = false, length = 24)
    @Builder.Default
    private String mode = "CLASSIC";

    @Column(name = "daily", nullable = false)
    @Builder.Default
    private boolean daily = false;

    @Column(name = "hints_used", nullable = false)
    @Builder.Default
    private int hintsUsed = 0;

    @Column(name = "mistakes", nullable = false)
    @Builder.Default
    private int mistakes = 0;

    @Column(name = "completion_time_seconds")
    private Integer completionTimeSeconds;

    /**
     * Optimistic-lock version column.
     * Incremented by Hibernate on every UPDATE, preventing lost-update anomalies
     * under concurrent access without requiring a database-level row lock.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Move> moves = new ArrayList<>();
}
