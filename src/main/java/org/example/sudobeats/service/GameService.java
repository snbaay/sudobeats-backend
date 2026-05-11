package org.example.sudobeats.service;

import org.example.sudobeats.dto.request.CreateGameRequest;
import org.example.sudobeats.dto.request.MakeMoveRequest;
import org.example.sudobeats.dto.response.GameResponse;
import org.example.sudobeats.dto.response.MoveResponse;
import org.example.sudobeats.dto.response.TrackResponse;
import org.example.sudobeats.engine.SudokuEngine;
import org.example.sudobeats.entity.Move;
import org.example.sudobeats.entity.SudokuGame;
import org.example.sudobeats.entity.User;
import org.example.sudobeats.entity.enums.GameStatus;
import org.example.sudobeats.exception.GameAlreadyCompletedException;
import org.example.sudobeats.exception.GameNotFoundException;
import org.example.sudobeats.exception.InvalidMoveException;
import org.example.sudobeats.exception.UserNotFoundException;
import org.example.sudobeats.mapper.GameMapper;
import org.example.sudobeats.mapper.MoveMapper;
import org.example.sudobeats.repository.MoveRepository;
import org.example.sudobeats.repository.SudokuGameRepository;
import org.example.sudobeats.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Core game logic service.
 *
 * <h3>Thread-safety strategy (two-layer)</h3>
 * <ol>
 *   <li><b>In-process lock</b> — a per-game {@link ReentrantLock} stored in
 *       {@code gameLocks} ensures that concurrent requests for the same game
 *       within this JVM instance are serialised, preventing race conditions on
 *       {@code currentBoard} between the read and the write.</li>
 *   <li><b>Database-level optimistic lock</b> — the {@code @Version} column on
 *       {@link SudokuGame} catches concurrent writes that slip through (e.g.
 *       in a multi-node deployment) and surfaces them as a clean
 *       {@code 409 Conflict} via {@linkGlobalExceptionHandler}.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final SudokuGameRepository gameRepository;
    private final MoveRepository       moveRepository;
    private final UserRepository       userRepository;
    private final SudokuEngine         sudokuEngine;
    private final GameMapper           gameMapper;
    private final MoveMapper           moveMapper;

    /**
     * Per-game locks.  ConcurrentHashMap.computeIfAbsent is itself atomic,
     * so two threads racing to create the lock for a new gameId will always
     * end up with the same lock instance.
     */
    private final ConcurrentHashMap<UUID, ReentrantLock> gameLocks = new ConcurrentHashMap<>();

    // ── Game lifecycle ────────────────────────────────────────────────────────

    /**
     * Generates a new Sudoku puzzle and persists the game session.
     * The solution board is stored server-side and never returned to the client.
     */
    @Transactional
    public GameResponse createGame(CreateGameRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId().toString()));

        int[][] solution    = sudokuEngine.generateSolution();
        int[][] initialBoard = sudokuEngine.generatePuzzle(solution, request.difficulty());
        int[][] currentBoard = sudokuEngine.deepCopy(initialBoard);

        SudokuGame game = SudokuGame.builder()
                .user(user)
                .initialBoard(initialBoard)
                .currentBoard(currentBoard)
                .solutionBoard(solution)
                .difficulty(request.difficulty())
                .status(GameStatus.IN_PROGRESS)
                .build();

        SudokuGame saved = gameRepository.save(game);
        log.info("Game [{}] created for user [{}] — difficulty={}", saved.getId(), user.getUsername(), request.difficulty());
        return gameMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public GameResponse getGame(UUID gameId) {
        SudokuGame game = fetchGame(gameId);
        return gameMapper.toResponse(game);
    }

    /**
     * Abandons an in-progress game.  Completed games cannot be abandoned.
     */
    @Transactional
    public GameResponse abandonGame(UUID gameId) {
        SudokuGame game = fetchGame(gameId);
        if (game.getStatus() == GameStatus.COMPLETED) {
            throw new GameAlreadyCompletedException(gameId.toString());
        }
        game.setStatus(GameStatus.ABANDONED);
        return gameMapper.toResponse(gameRepository.save(game));
    }

    // ── Move processing ───────────────────────────────────────────────────────

    /**
     * Validates and records a player's move.
     *
     * <h4>Validation sequence</h4>
     * <ol>
     *   <li>Game must be IN_PROGRESS.</li>
     *   <li>Target cell must not be a clue (initialBoard[row][col] != 0).</li>
     *   <li>Placement must not violate row / column / box uniqueness constraints.</li>
     * </ol>
     *
     * After a successful move the service checks whether the board is complete
     * and transitions the game to COMPLETED if so.
     */
    @Transactional
    public MoveResponse makeMove(UUID gameId, MakeMoveRequest request) {
        ReentrantLock lock = gameLocks.computeIfAbsent(gameId, id -> new ReentrantLock());
        lock.lock();
        try {
            return doMakeMove(gameId, request);
        } finally {
            lock.unlock();
        }
    }

    /** Called inside the per-game lock. */
    private MoveResponse doMakeMove(UUID gameId, MakeMoveRequest request) {
        SudokuGame game = fetchGame(gameId);

        // ── Guard: game must be active ─────────────────────────────────────
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new GameAlreadyCompletedException(gameId.toString());
        }

        int row   = request.row();
        int col   = request.col();
        int value = request.value();

        int[][] initialBoard = game.getInitialBoard();
        int[][] currentBoard = game.getCurrentBoard();
        int[][] solutionBoard = game.getSolutionBoard();

        // ── Guard: cannot overwrite a clue cell ────────────────────────────
        if (initialBoard[row][col] != 0) {
            throw new InvalidMoveException(
                    "cell (%d,%d) is a puzzle clue and cannot be overwritten".formatted(row, col)
            );
        }

        // ── Guard: must not violate Sudoku constraints ─────────────────────
        if (!sudokuEngine.isValidPlacement(currentBoard, row, col, value)) {
            throw new InvalidMoveException(
                    "value %d conflicts with an existing digit in row %d, column %d, or its 3×3 box"
                            .formatted(value, row, col)
            );
        }

        // ── Apply the move ─────────────────────────────────────────────────
        currentBoard[row][col] = value;
        game.setCurrentBoard(currentBoard);

        boolean correct = sudokuEngine.isCorrectPlacement(solutionBoard, row, col, value);
        long    tsMs    = Instant.now().toEpochMilli() - game.getStartedAt().toEpochMilli();

        Move move = Move.builder()
                .game(game)
                .row(row)
                .col(col)
                .value(value)
                .timestampFromStart(tsMs)
                .correct(correct)
                .build();

        // ── Check for game completion ──────────────────────────────────────
        if (sudokuEngine.isBoardComplete(currentBoard, solutionBoard)) {
            game.setStatus(GameStatus.COMPLETED);
            game.setCompletedAt(Instant.now());
            log.info("Game [{}] completed by user [{}] in {}ms",
                    gameId, game.getUser().getUsername(), tsMs);
        }

        gameRepository.save(game);
        Move savedMove = moveRepository.save(move);
        log.debug("Move saved: game={} cell=({},{}) value={} correct={}", gameId, row, col, value, correct);

        return moveMapper.toResponse(savedMove);
    }

    // ── Move history ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MoveResponse> getMoves(UUID gameId) {
        ensureGameExists(gameId);
        return moveMapper.toResponseList(
                moveRepository.findByGameIdOrderByTimestampFromStartAsc(gameId)
        );
    }

    // ── Music Metadata API ────────────────────────────────────────────────────

    /**
     * Returns the CORRECT moves for a game, sorted by {@code timestampFromStart}.
     * Each move maps to a musical note (digit 1–9 → scale degree).
     * The timestamp is the x-axis; the value (1–9) is the pitch.
     * Together they reconstruct the melody the player "composed" while solving.
     */
    @Transactional(readOnly = true)
    public TrackResponse getTrack(UUID gameId) {
        SudokuGame game = fetchGame(gameId);
        List<Move> correctMoves =
                moveRepository.findCorrectMovesByGameIdOrderByTimestamp(gameId);

        long totalDurationMs = correctMoves.isEmpty() ? 0 :
                correctMoves.get(correctMoves.size() - 1).getTimestampFromStart();

        List<MoveResponse> noteDtos = moveMapper.toResponseList(correctMoves);

        return new TrackResponse(
                game.getId(),
                game.getDifficulty(),
                correctMoves.size(),
                totalDurationMs,
                noteDtos
        );
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private SudokuGame fetchGame(UUID gameId) {
        return gameRepository.findByIdWithUser(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId.toString()));
    }

    private void ensureGameExists(UUID gameId) {
        if (!gameRepository.existsById(gameId)) {
            throw new GameNotFoundException(gameId.toString());
        }
    }
}
