package org.example.sudobeats.repository;

import org.example.sudobeats.entity.Move;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MoveRepository extends JpaRepository<Move, UUID> {

    /** All moves for a game in chronological order. */
    List<Move> findByGameIdOrderByTimestampFromStartAsc(UUID gameId);

    /**
     * Only the CORRECT moves, sorted by timestamp — these are the "notes"
     * used to reconstruct the melody in the /track endpoint.
     */
    @Query("""
            SELECT m FROM Move m
            WHERE m.game.id = :gameId
              AND m.correct = true
            ORDER BY m.timestampFromStart ASC
            """)
    List<Move> findCorrectMovesByGameIdOrderByTimestamp(@Param("gameId") UUID gameId);

    /** How many moves a player has made in a game. */
    long countByGameId(UUID gameId);

    /** How many correct moves exist so far. */
    long countByGameIdAndCorrect(UUID gameId, boolean correct);
}
