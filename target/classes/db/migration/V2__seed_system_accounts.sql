-- System accounts (fixed UUIDs for config). wallet_id = NULL.
-- Initial ledger entries: each system account gets 1000000000 opening balance (double-entry).

-- Allow OPENING_BALANCE and SYSTEM_CREDIT transaction types
ALTER TABLE transaction DROP CONSTRAINT IF EXISTS chk_txn_type;
ALTER TABLE transaction ADD CONSTRAINT chk_txn_type CHECK (txn_type IN (
    'CASH_IN', 'CASH_OUT', 'TRANSFER', 'REVERSAL', 'WITHDRAWAL_RESERVE', 'WITHDRAWAL_SETTLE', 'OPENING_BALANCE', 'SYSTEM_CREDIT'
));

INSERT INTO account (account_id, account_type, wallet_id, status) VALUES
    ('00000000-0000-0000-0000-000000000001', 'SYSTEM_MASTER_ACCOUNT', NULL, 'ACTIVE'),
    ('00000000-0000-0000-0000-000000000002', 'SETTLEMENT_ACCOUNT', NULL, 'ACTIVE'),
    ('00000000-0000-0000-0000-000000000003', 'WITHDRAWAL_PENDING_ACCOUNT', NULL, 'ACTIVE'),
    ('00000000-0000-0000-0000-000000000004', 'FEE_ACCOUNT', NULL, 'ACTIVE'),
    ('00000000-0000-0000-0000-000000000005', 'REVERSAL_ACCOUNT', NULL, 'ACTIVE')
ON CONFLICT (account_id) DO NOTHING;

-- Genesis transaction: allocate 1000000000 to each of SETTLEMENT, WITHDRAWAL_PENDING, FEE, REVERSAL (DEBIT SYSTEM_MASTER 4x, CREDIT each 1x).
-- Then CREDIT SYSTEM_MASTER 4000000000 so SYSTEM_MASTER ends at 0. Order of legs for balance_after:
-- 1. DEBIT SYSTEM_MASTER 4000000000 -> balance_after = -4000000000
-- 2. CREDIT SETTLEMENT 1000000000 -> 1000000000
-- 3. CREDIT WITHDRAWAL_PENDING 1000000000 -> 1000000000
-- 4. CREDIT FEE 1000000000 -> 1000000000
-- 5. CREDIT REVERSAL 1000000000 -> 1000000000
-- 6. CREDIT SYSTEM_MASTER 4000000000 -> -4000000000 + 4000000000 = 0

INSERT INTO transaction (txn_id, txn_type, status, reference_id, created_at) VALUES
    ('00000000-0000-0000-0000-000000000000', 'OPENING_BALANCE', 'COMPLETED', 'genesis-system-accounts', clock_timestamp())
ON CONFLICT (txn_id) DO NOTHING;

INSERT INTO ledger_entry (entry_id, txn_id, account_id, direction, amount, balance_after, created_at)
SELECT gen_random_uuid(), '00000000-0000-0000-0000-000000000000'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'DEBIT',  4000000000, -4000000000, clock_timestamp()
WHERE NOT EXISTS (SELECT 1 FROM ledger_entry WHERE txn_id = '00000000-0000-0000-0000-000000000000'::uuid AND account_id = '00000000-0000-0000-0000-000000000001'::uuid AND direction = 'DEBIT');
INSERT INTO ledger_entry (entry_id, txn_id, account_id, direction, amount, balance_after, created_at)
SELECT gen_random_uuid(), '00000000-0000-0000-0000-000000000000'::uuid, '00000000-0000-0000-0000-000000000002'::uuid, 'CREDIT', 1000000000, 1000000000, clock_timestamp()
WHERE NOT EXISTS (SELECT 1 FROM ledger_entry WHERE txn_id = '00000000-0000-0000-0000-000000000000'::uuid AND account_id = '00000000-0000-0000-0000-000000000002'::uuid AND direction = 'CREDIT');
INSERT INTO ledger_entry (entry_id, txn_id, account_id, direction, amount, balance_after, created_at)
SELECT gen_random_uuid(), '00000000-0000-0000-0000-000000000000'::uuid, '00000000-0000-0000-0000-000000000003'::uuid, 'CREDIT', 1000000000, 1000000000, clock_timestamp()
WHERE NOT EXISTS (SELECT 1 FROM ledger_entry WHERE txn_id = '00000000-0000-0000-0000-000000000000'::uuid AND account_id = '00000000-0000-0000-0000-000000000003'::uuid AND direction = 'CREDIT');
INSERT INTO ledger_entry (entry_id, txn_id, account_id, direction, amount, balance_after, created_at)
SELECT gen_random_uuid(), '00000000-0000-0000-0000-000000000000'::uuid, '00000000-0000-0000-0000-000000000004'::uuid, 'CREDIT', 1000000000, 1000000000, clock_timestamp()
WHERE NOT EXISTS (SELECT 1 FROM ledger_entry WHERE txn_id = '00000000-0000-0000-0000-000000000000'::uuid AND account_id = '00000000-0000-0000-0000-000000000004'::uuid AND direction = 'CREDIT');
INSERT INTO ledger_entry (entry_id, txn_id, account_id, direction, amount, balance_after, created_at)
SELECT gen_random_uuid(), '00000000-0000-0000-0000-000000000000'::uuid, '00000000-0000-0000-0000-000000000005'::uuid, 'CREDIT', 1000000000, 1000000000, clock_timestamp()
WHERE NOT EXISTS (SELECT 1 FROM ledger_entry WHERE txn_id = '00000000-0000-0000-0000-000000000000'::uuid AND account_id = '00000000-0000-0000-0000-000000000005'::uuid AND direction = 'CREDIT');
INSERT INTO ledger_entry (entry_id, txn_id, account_id, direction, amount, balance_after, created_at)
SELECT gen_random_uuid(), '00000000-0000-0000-0000-000000000000'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'CREDIT', 4000000000, 0, clock_timestamp()
WHERE NOT EXISTS (SELECT 1 FROM ledger_entry WHERE txn_id = '00000000-0000-0000-0000-000000000000'::uuid AND account_id = '00000000-0000-0000-0000-000000000001'::uuid AND direction = 'CREDIT');
