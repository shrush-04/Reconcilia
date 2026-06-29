package com.reconcilia.dto;

/**
 * Response body returned by {@code POST /api/statements/upload}.
 *
 * @param bank          source bank name (e.g., "ALPHA", "BETA")
 * @param fileName      original filename of the uploaded CSV
 * @param savedCount    number of new transactions persisted in this request
 * @param duplicateCount number of rows skipped because their referenceNumber already exists
 */
public record IngestionResult(
        String bank,
        String fileName,
        int savedCount,
        int duplicateCount
) {}
