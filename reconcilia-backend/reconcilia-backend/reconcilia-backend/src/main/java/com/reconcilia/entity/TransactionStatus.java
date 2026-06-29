package com.reconcilia.entity;

/**
 * Lifecycle status of a persisted transaction.
 *
 * <ul>
 *   <li>{@link #PENDING}   – row parsed and saved but not yet reconciled.</li>
 *   <li>{@link #PROCESSED} – row has been successfully reconciled.</li>
 * </ul>
 */
public enum TransactionStatus {
    PENDING,
    PROCESSED
}
