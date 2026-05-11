package org.example.sudobeats.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InvalidMoveException extends RuntimeException {
    public InvalidMoveException(String reason) {
        super("Invalid move: " + reason);
    }
}
