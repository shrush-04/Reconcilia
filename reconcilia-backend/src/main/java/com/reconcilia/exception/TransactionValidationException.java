package com.reconcilia.exception;

import java.util.List;

/**
 * Thrown when one or more parsed {@code Transaction} objects fail Bean Validation.
 * Collects all row-level errors before throwing so the caller gets the full picture
 * in a single response rather than failing on the first bad row.
 *
 * <p>Maps to HTTP 400 Bad Request via {@link GlobalExceptionHandler}.
 */
public class TransactionValidationException extends RuntimeException {

    private final List<String> errors;

    public TransactionValidationException(List<String> errors) {
        super("Validation failed for " + errors.size() + " row(s)");
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
