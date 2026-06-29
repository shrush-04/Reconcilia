package com.reconcilia.exception;

/**
 * Thrown when a CSV row cannot be parsed due to a malformed value
 * (e.g., unparseable date, non-numeric amount).
 *
 * <p>Maps to HTTP 400 Bad Request via {@link GlobalExceptionHandler}.
 */
public class CsvParseException extends RuntimeException {

    public CsvParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
