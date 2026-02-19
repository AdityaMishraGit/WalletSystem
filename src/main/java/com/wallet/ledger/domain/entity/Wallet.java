package com.wallet.ledger.domain.entity;

import com.wallet.ledger.domain.valueobject.WalletId;
import com.wallet.ledger.domain.valueobject.WalletStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Wallet aggregate root. Balance is never stored; derived from ledger_entry.balance_after.
 */
@Value
@Builder
public class Wallet {

    WalletId walletId;
    String userId;
    WalletStatus status;
    String currency;
    Instant createdAt;

    public WalletId getWalletId() { return walletId; }
    public String getUserId() { return userId; }
    public WalletStatus getStatus() { return status; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }

    public boolean isActive() {
        return status == WalletStatus.ACTIVE;
    }
}
