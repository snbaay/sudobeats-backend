package org.example.sudobeats.repository;

import org.example.sudobeats.entity.SudokuGame;
import org.example.sudobeats.entity.enums.Difficulty;
import org.example.sudobeats.entity.enums.GameStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SudokuGameRepository extends JpaRepository<SudokuGame, UUID> {

    /** All games for a specific user, newest first. */
    Page<SudokuGame> findByUserIdOrderByStartedAtDesc(UUID userId, Pageable pageable);

    /** Active (in-progress) games for a user. */
    List<SudokuGame> findByUserIdAndStatus(UUID userId, GameStatus status);

    /** Used to enforce one active game per user if desired. */
    boolean existsByUserIdAndStatus(UUID userId, GameStatus status);

    /** Fetch a game and eagerly load its user to avoid N+1 on the controller layer. */
    @Query("SELECT g FROM SudokuGame g JOIN FETCH g.user WHERE g.id = :id")
    Optional<SudokuGame> findByIdWithUser(@Param("id") UUID id);

    long countByUserIdAndDifficulty(UUID userId, Difficulty difficulty);
}
