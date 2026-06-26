-- Flyway migration V1: initial schema for the Reconcilia reconciliation engine
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS transactions
(
    id               BIGSERIAL PRIMARY KEY,
    account_id       VARCHAR(100)   NOT NULL,
    transaction_date DATE           NOT NULL,
    -- Signed amount: positive = credit (money in), negative = debit (money out)
    amount           NUMERIC(19, 4) NOT NULL,
    description      VARCHAR(500),
    -- Globally unique identifier supplied by the source bank
    reference_number VARCHAR(100)   NOT NULL,
    -- Enum values: ALPHA, BETA (expandable)
    source_bank      VARCHAR(20)    NOT NULL,
    -- Enum values: PENDING, PROCESSED
    status           VARCHAR(20)    NOT NULL
);

-- Unique index on reference_number enforces the duplicate-detection guarantee at
-- the database level as a safety net (the service layer checks first).
CREATE UNIQUE INDEX IF NOT EXISTS uidx_transactions_reference_number
    ON transactions (reference_number);

-- Index to support efficient lookups by account and date range
CREATE INDEX IF NOT EXISTS idx_transactions_account_date
    ON transactions (account_id, transaction_date);
