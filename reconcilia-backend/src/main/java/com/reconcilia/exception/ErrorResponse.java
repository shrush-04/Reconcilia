package com.reconcilia.exception;

import java.util.List;

/**
 * Standard error body returned by {@link GlobalExceptionHandler} for all 4xx/5xx responses.
 */
public record ErrorResponse(String message, List<String> details) {

    /** Convenience constructor for errors with no detail list. */
    public ErrorResponse(String message) {
        this(message, List.of());
    }
}
