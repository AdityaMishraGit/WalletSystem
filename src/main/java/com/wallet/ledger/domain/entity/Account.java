package com.wallet.ledger.domain.entity;

import com.wallet.ledger.domain.valueobject.AccountId;
import com.wallet.ledger.domain.valueobject.AccountStatus;
import com.wallet.ledger.domain.valueobject.AccountType;
import com.wallet.ledger.domain.valueobject.WalletId;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;

/**
 * Account entity. System accounts have walletId null. Balance derived from ledger entries.
 */
@Value
@Builder
public class Account {

    AccountId accountId;
    AccountType accountType;
    AccountStatus status;
    WalletId walletId;

    public boolean isSystemAccount() {
        return walletId == null;
    }

    public Optional<WalletId> getWalletIdOptional() {
        return Optional.ofNullable(walletId);
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }
}
