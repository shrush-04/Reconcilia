package com.reconcilia.dto;

/**
 * Summary result of a reconciliation run.
 *
 * @param totalProcessed total number of transactions evaluated
 * @param matchedCount   number of transactions successfully matched
 * @param unmatchedCount number of transactions left unmatched
 * @param duplicateCount number of duplicate transactions detected
 */
public record ReconciliationSummary(
        int totalProcessed,
        int matchedCount,
        int unmatchedCount,
        int duplicateCount
) {}
