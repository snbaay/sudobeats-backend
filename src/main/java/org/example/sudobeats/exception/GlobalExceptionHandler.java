package org.example.sudobeats.exception;


import org.example.sudobeats.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * Catches every exception that bubbles out of controllers and converts it into
 * a consistent {@link ApiResponse} JSON body.  No raw stack traces ever reach the client.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── 404 Not Found ─────────────────────────────────────────────────────────

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleGameNotFound(GameNotFoundException ex) {
        return respond(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
        return respond(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 409 Conflict ──────────────────────────────────────────────────────────

    @ExceptionHandler(GameAlreadyCompletedException.class)
    public ResponseEntity<ApiResponse<Void>> handleGameCompleted(GameAlreadyCompletedException ex) {
        return respond(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateResourceException ex) {
        return respond(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── 422 Unprocessable Entity ───────────────────────────────────────────────

    @ExceptionHandler(InvalidMoveException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidMove(InvalidMoveException ex) {
        return respond(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    // ── 400 Bad Request ───────────────────────────────────────────────────────

    /**
     * Fires when @Valid fails on a @RequestBody — aggregates all field errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Fires when @Validated fails on path/query params.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Fires when request JSON is malformed or an enum value is unknown.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        return respond(HttpStatus.BAD_REQUEST, "Malformed request body — check JSON syntax and enum values");
    }

    /**
     * Fires when a path variable can't be converted (e.g. a non-UUID string where UUID is expected).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    // ── 409 Optimistic Lock ───────────────────────────────────────────────────

    /**
     * Fires when two concurrent writes hit the same game row.
     * The client should retry the move.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        log.warn("Optimistic lock conflict on {}", ex.getPersistentClassName());
        return respond(HttpStatus.CONFLICT, "Concurrent update detected — please retry your move");
    }

    // ── 500 Catch-all ─────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    // ─────────────────────────────────────────────────────────────────────────

    private ResponseEntity<ApiResponse<Void>> respond(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.error(message));
    }
}