-- Flyway migration V2: drop the unique reference number constraint/index to support duplicate transactions
-- ─────────────────────────────────────────────────────────────────────────────

DROP INDEX IF EXISTS uidx_transactions_reference_number;

CREATE INDEX IF NOT EXISTS idx_transactions_reference_number
    ON transactions (reference_number);
