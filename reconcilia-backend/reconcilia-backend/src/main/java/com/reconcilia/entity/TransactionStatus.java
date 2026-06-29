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
<<<<<<< HEAD
    MATCHED,
    UNMATCHED,
    DUPLICATE
=======
    PROCESSED
>>>>>>> e815eb3 (solve the backend doker rleated error)
}
