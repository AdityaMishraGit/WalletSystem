-- Provisioning services and bundles for TV recharge, prepaid, subscriptions, etc.
-- Transaction table extended for PROVISIONING txn type and bundle/reference.

-- Service types: TV_RECHARGE, PREPAID_RECHARGE, POSTPAID_RECHARGE, SUBSCRIPTION, DATA_BUNDLE, UTILITY_BILL
CREATE TABLE IF NOT EXISTS provisioning_service (
    id          UUID         NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    type        VARCHAR(64)  NOT NULL,
    name        VARCHAR(255) NOT NULL,
    code        VARCHAR(64)  NOT NULL,
    subcategory VARCHAR(128) NULL,
    description VARCHAR(512) NULL,
    status      VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT chk_provisioning_service_type CHECK (type IN (
        'TV_RECHARGE', 'PREPAID_RECHARGE', 'POSTPAID_RECHARGE', 'SUBSCRIPTION', 'DATA_BUNDLE', 'UTILITY_BILL'
    )),
    CONSTRAINT chk_provisioning_service_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_provisioning_service_code ON provisioning_service (code);
CREATE INDEX IF NOT EXISTS idx_provisioning_service_type ON provisioning_service (type);
CREATE INDEX IF NOT EXISTS idx_provisioning_service_subcategory ON provisioning_service (subcategory);

CREATE TABLE IF NOT EXISTS service_bundle (
    id                     UUID          NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    provisioning_service_id UUID         NOT NULL,
    name                   VARCHAR(255)  NOT NULL,
    code                   VARCHAR(64)  NOT NULL,
    amount_min             NUMERIC(19, 4) NULL,
    amount_max             NUMERIC(19, 4) NULL,
    fixed_amount           NUMERIC(19, 4) NULL,
    subcategory            VARCHAR(128)  NULL,
    validity_days          INT          NULL,
    description            VARCHAR(512) NULL,
    status                 VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT fk_bundle_service FOREIGN KEY (provisioning_service_id) REFERENCES provisioning_service (id),
    CONSTRAINT chk_bundle_amount CHECK (
        (fixed_amount IS NOT NULL AND fixed_amount >= 0) OR
        (amount_min IS NOT NULL AND amount_max IS NOT NULL AND amount_min >= 0 AND amount_max >= amount_min)
    ),
    CONSTRAINT chk_bundle_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX IF NOT EXISTS idx_service_bundle_service_id ON service_bundle (provisioning_service_id);
CREATE INDEX IF NOT EXISTS idx_service_bundle_subcategory ON service_bundle (subcategory);
CREATE INDEX IF NOT EXISTS idx_service_bundle_amounts ON service_bundle (COALESCE(fixed_amount, amount_min), COALESCE(fixed_amount, amount_max));

-- Extend transaction for provisioning
ALTER TABLE transaction ADD COLUMN IF NOT EXISTS service_bundle_id UUID NULL;
ALTER TABLE transaction ADD COLUMN IF NOT EXISTS provisioning_reference VARCHAR(512) NULL;
ALTER TABLE transaction DROP CONSTRAINT IF EXISTS chk_txn_type;
ALTER TABLE transaction ADD CONSTRAINT chk_txn_type CHECK (txn_type IN (
    'CASH_IN', 'CASH_OUT', 'TRANSFER', 'REVERSAL', 'WITHDRAWAL_RESERVE', 'WITHDRAWAL_SETTLE', 'OPENING_BALANCE', 'SYSTEM_CREDIT', 'PROVISIONING'
));

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_transaction_service_bundle') THEN
        ALTER TABLE transaction ADD CONSTRAINT fk_transaction_service_bundle
            FOREIGN KEY (service_bundle_id) REFERENCES service_bundle (id);
    END IF;
END $$;

-- Seed sample services and bundles (idempotent)
INSERT INTO provisioning_service (id, type, name, code, subcategory, description, status) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'TV_RECHARGE', 'DSTv', 'DSTV', 'entertainment', 'DSTv TV recharge', 'ACTIVE'),
    ('a1000000-0000-0000-0000-000000000002', 'PREPAID_RECHARGE', 'Airtel Prepaid', 'AIRTEL_PREPAID', 'mobile', 'Airtel prepaid recharge', 'ACTIVE'),
    ('a1000000-0000-0000-0000-000000000003', 'SUBSCRIPTION', 'Netflix', 'NETFLIX', 'entertainment', 'Netflix subscription', 'ACTIVE'),
    ('a1000000-0000-0000-0000-000000000004', 'DATA_BUNDLE', 'Mobile Data', 'DATA_BUNDLE', 'mobile', 'Mobile data bundles', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO service_bundle (id, provisioning_service_id, name, code, fixed_amount, subcategory, validity_days, description, status) VALUES
    ('b2000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', 'DSTv Compact', 'DSTV_COMPACT', 50.00, 'entertainment', 30, 'DSTv Compact monthly', 'ACTIVE'),
    ('b2000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'DSTv Premium', 'DSTV_PREMIUM', 100.00, 'entertainment', 30, 'DSTv Premium monthly', 'ACTIVE'),
    ('b2000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000002', 'Airtel 5 GB', 'AIRTEL_5GB', 10.00, 'mobile', 30, '5 GB data', 'ACTIVE'),
    ('b2000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000002', 'Airtel 20 GB', 'AIRTEL_20GB', 25.00, 'mobile', 30, '20 GB data', 'ACTIVE'),
    ('b2000000-0000-0000-0000-000000000005', 'a1000000-0000-0000-0000-000000000003', 'Netflix Basic', 'NETFLIX_BASIC', 9.99, 'entertainment', 30, 'Netflix Basic monthly', 'ACTIVE'),
    ('b2000000-0000-0000-0000-000000000006', 'a1000000-0000-0000-0000-000000000003', 'Netflix Premium', 'NETFLIX_PREMIUM', 19.99, 'entertainment', 30, 'Netflix Premium monthly', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;
