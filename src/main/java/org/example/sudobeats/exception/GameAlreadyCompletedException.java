package org.example.sudobeats.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class GameAlreadyCompletedException extends RuntimeException {
    public GameAlreadyCompletedException(String gameId) {
        super("Game is already finished: " + gameId);
    }
}
