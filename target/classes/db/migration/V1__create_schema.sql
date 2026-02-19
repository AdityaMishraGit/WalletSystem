-- Wallet Ledger - Double-entry schema
-- Balance is NEVER stored; derived from ledger_entry.balance_after
-- Idempotent: safe to run when objects already exist (e.g. after a failed run).

CREATE TABLE IF NOT EXISTS wallet (
    wallet_id   UUID         NOT NULL PRIMARY KEY,
    user_id     VARCHAR(255) NOT NULL,
    status      VARCHAR(32)  NOT NULL,
    currency    VARCHAR(3)   NOT NULL DEFAULT 'USD',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT chk_wallet_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED'))
);

CREATE INDEX IF NOT EXISTS idx_wallet_user_id ON wallet (user_id);
CREATE INDEX IF NOT EXISTS idx_wallet_status ON wallet (status);

CREATE TABLE IF NOT EXISTS account (
    account_id   UUID         NOT NULL PRIMARY KEY,
    account_type VARCHAR(64)  NOT NULL,
    wallet_id    UUID         NULL,
    status       VARCHAR(32)  NOT NULL,
    CONSTRAINT chk_account_type CHECK (account_type IN (
        'SYSTEM_MASTER_ACCOUNT', 'USER_WALLET_ACCOUNT', 'SETTLEMENT_ACCOUNT',
        'WITHDRAWAL_PENDING_ACCOUNT', 'FEE_ACCOUNT', 'REVERSAL_ACCOUNT'
    )),
    CONSTRAINT chk_account_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED'))
);

CREATE INDEX IF NOT EXISTS idx_account_wallet_id ON account (wallet_id);
CREATE INDEX IF NOT EXISTS idx_account_type ON account (account_type);
CREATE UNIQUE INDEX IF NOT EXISTS idx_account_wallet_type ON account (wallet_id, account_type) WHERE wallet_id IS NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_account_wallet') THEN
        ALTER TABLE account ADD CONSTRAINT fk_account_wallet FOREIGN KEY (wallet_id) REFERENCES wallet (wallet_id);
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS transaction (
    txn_id       UUID         NOT NULL PRIMARY KEY,
    txn_type     VARCHAR(32)  NOT NULL,
    status       VARCHAR(32)  NOT NULL,
    reference_id VARCHAR(255) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT chk_txn_type CHECK (txn_type IN (
        'CASH_IN', 'CASH_OUT', 'TRANSFER', 'REVERSAL', 'WITHDRAWAL_RESERVE', 'WITHDRAWAL_SETTLE'
    )),
    CONSTRAINT chk_txn_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REVERSED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_transaction_reference_id ON transaction (reference_id);
CREATE INDEX IF NOT EXISTS idx_transaction_created_at ON transaction (created_at);
CREATE INDEX IF NOT EXISTS idx_transaction_type ON transaction (txn_type);

CREATE TABLE IF NOT EXISTS ledger_entry (
    entry_id      UUID          NOT NULL PRIMARY KEY,
    txn_id        UUID          NOT NULL,
    account_id    UUID          NOT NULL,
    direction     VARCHAR(8)    NOT NULL,
    amount        NUMERIC(19, 4) NOT NULL,
    balance_after NUMERIC(19, 4) NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT chk_direction CHECK (direction IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_amount_positive CHECK (amount >= 0),
    CONSTRAINT fk_ledger_txn FOREIGN KEY (txn_id) REFERENCES transaction (txn_id),
    CONSTRAINT fk_ledger_account FOREIGN KEY (account_id) REFERENCES account (account_id)
);

CREATE INDEX IF NOT EXISTS idx_ledger_entry_txn_id ON ledger_entry (txn_id);
CREATE INDEX IF NOT EXISTS idx_ledger_entry_account_id ON ledger_entry (account_id);
CREATE INDEX IF NOT EXISTS idx_ledger_entry_account_created ON ledger_entry (account_id, created_at DESC);

COMMENT ON TABLE ledger_entry IS 'Immutable double-entry postings. Balance = latest balance_after per account.';
