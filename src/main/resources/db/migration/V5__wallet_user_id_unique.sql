-- userId is unique: one wallet per user. Transactional APIs use userId as the primary key.
CREATE UNIQUE INDEX IF NOT EXISTS idx_wallet_user_id_unique ON wallet (user_id);
