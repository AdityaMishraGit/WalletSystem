package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.entity.Wallet;

public interface SaveWalletPort {
    Wallet save(Wallet wallet);
}
