package org.example.sudobeats.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Records a single cell placement made by the player.
 *
 * timestampFromStart (milliseconds since game creation) is used by the
 * /api/games/{id}/track endpoint to reconstruct the musical melody in
 * chronological order.
 *
 * isCorrect is set at move-time by comparing against solutionBoard —
 * only correct moves form the playable melody.
 */
@Entity
@Table(
        name = "moves",
        indexes = {
                @Index(name = "idx_moves_game_id",    columnList = "game_id"),
                @Index(name = "idx_moves_timestamp",  columnList = "timestamp_from_start"),
                @Index(name = "idx_moves_is_correct", columnList = "is_correct")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Move {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private SudokuGame game;

    /** 0-indexed row (0 = top, 8 = bottom). */
    @Column(name = "row_index", nullable = false)
    private int row;

    /** 0-indexed column (0 = left, 8 = right). */
    @Column(name = "col_index", nullable = false)
    private int col;

    /** The digit placed by the player (1–9). */
    @Column(name = "value", nullable = false)
    private int value;

    /** Milliseconds elapsed since the game started. */
    @Column(name = "timestamp_from_start", nullable = false)
    private long timestampFromStart;

    /**
     * True when the placed value matches solutionBoard[row][col].
     * Correct moves are the "notes" of the melody returned by /track.
     */
    @Column(name = "is_correct", nullable = false)
    private boolean correct;
}
