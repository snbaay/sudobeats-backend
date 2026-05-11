package org.example.sudobeats.controller;

import org.example.sudobeats.dto.request.CreateGameRequest;
import org.example.sudobeats.dto.request.MakeMoveRequest;
import org.example.sudobeats.dto.response.ApiResponse;
import org.example.sudobeats.dto.response.GameResponse;
import org.example.sudobeats.dto.response.MoveResponse;
import org.example.sudobeats.dto.response.TrackResponse;
import org.example.sudobeats.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for all Sudoku game operations.
 *
 * <pre>
 * POST   /api/games                    → create a new game session
 * GET    /api/games/{id}               → get game state
 * DELETE /api/games/{id}               → abandon game
 * POST   /api/games/{id}/moves         → submit a move
 * GET    /api/games/{id}/moves         → get all moves (full history)
 * GET    /api/games/{id}/track         → get music track (correct moves only)
 * </pre>
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    // ── Game lifecycle ────────────────────────────────────────────────────────

    /**
     * POST /api/games
     * Creates a new puzzle for the given user + difficulty.
     * Returns the game board — solution board is intentionally withheld.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<GameResponse>> createGame(
            @Valid @RequestBody CreateGameRequest request) {

        GameResponse game = gameService.createGame(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(game, "Game created — good luck!"));
    }

    /**
     * GET /api/games/{id}
     * Returns the current state of the game (both initialBoard and currentBoard).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GameResponse>> getGame(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(gameService.getGame(id)));
    }

    /**
     * DELETE /api/games/{id}
     * Marks the game as ABANDONED.  Cannot abandon a COMPLETED game.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<GameResponse>> abandonGame(@PathVariable UUID id) {
        GameResponse game = gameService.abandonGame(id);
        return ResponseEntity.ok(ApiResponse.ok(game, "Game abandoned"));
    }

    // ── Move operations ───────────────────────────────────────────────────────

    /**
     * POST /api/games/{id}/moves
     * Submits a cell placement.  Validates Sudoku rules and rejects clue-cell overwrites.
     * Returns the recorded move — including whether it was correct — for immediate UI feedback.
     */
    @PostMapping("/{id}/moves")
    public ResponseEntity<ApiResponse<MoveResponse>> makeMove(
            @PathVariable UUID id,
            @Valid @RequestBody MakeMoveRequest request) {

        MoveResponse move = gameService.makeMove(id, request);
        String message = move.correct() ? "Correct move!" : "Move recorded";
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(move, message));
    }

    /**
     * GET /api/games/{id}/moves
     * Full chronological move history for replay or debugging.
     */
    @GetMapping("/{id}/moves")
    public ResponseEntity<ApiResponse<List<MoveResponse>>> getMoves(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(gameService.getMoves(id)));
    }

    // ── Music Metadata API ────────────────────────────────────────────────────

    /**
     * GET /api/games/{id}/track
     *
     * Returns a {@link TrackResponse} containing only the CORRECT moves,
     * sorted by {@code timestampFromStart} ascending.
     *
     * The front-end uses this to reconstruct the melody:
     * <ul>
     *   <li>{@code timestampFromStart} → note onset in milliseconds</li>
     *   <li>{@code value} (1–9)        → scale degree / pitch class</li>
     *   <li>{@code row}, {@code col}   → optional spatial panning / timbre</li>
     * </ul>
     *
     * This endpoint works on both IN_PROGRESS and COMPLETED games so the
     * front-end can preview the evolving melody in real time.
     */
    @GetMapping("/{id}/track")
    public ResponseEntity<ApiResponse<TrackResponse>> getTrack(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(gameService.getTrack(id)));
    }
}
