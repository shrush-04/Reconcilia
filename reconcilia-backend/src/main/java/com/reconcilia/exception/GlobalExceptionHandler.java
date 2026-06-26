package com.reconcilia.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;

/**
 * Centralised exception handler that maps domain exceptions to HTTP responses.
 * All error bodies use the {@link ErrorResponse} record for consistency.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Unknown bank identifier supplied in the query parameter → 400 */
    @ExceptionHandler(UnknownBankException.class)
    public ResponseEntity<ErrorResponse> handleUnknownBank(UnknownBankException ex) {
        log.warn("Unknown bank: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /** Malformed CSV value (bad date / amount) → 400 */
    @ExceptionHandler(CsvParseException.class)
    public ResponseEntity<ErrorResponse> handleCsvParse(CsvParseException ex) {
        log.warn("CSV parse failure: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /** One or more rows fail Bean Validation after parsing → 400 with per-row details */
    @ExceptionHandler(TransactionValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(TransactionValidationException ex) {
        log.warn("Transaction validation failed: {} error(s)", ex.getErrors().size());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage(), ex.getErrors()));
    }

    /** Empty file / bad parameter → 400 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /** Uploaded file exceeds spring.servlet.multipart.max-file-size → 400 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Uploaded file exceeds the maximum allowed size (10 MB)."));
    }

    /** IO errors reading the uploaded file stream → 500 */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIo(IOException ex) {
        log.error("IO error during file processing", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to read uploaded file: " + ex.getMessage()));
    }

    /** Catch-all for unexpected exceptions → 500 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred. Please try again later."));
    }
}
