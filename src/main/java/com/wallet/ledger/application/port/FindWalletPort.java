package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.entity.Wallet;
import com.wallet.ledger.domain.valueobject.WalletId;

import java.util.Optional;

public interface FindWalletPort {
    Optional<Wallet> findById(WalletId walletId);

    /** Find wallet by userId (unique). Primary key for transactional APIs. */
    Optional<Wallet> findByUserId(String userId);
}
