package com.wallet.ledger.domain.entity;

import com.wallet.ledger.domain.valueobject.AccountId;
import com.wallet.ledger.domain.valueobject.EntryDirection;
import com.wallet.ledger.domain.valueobject.EntryId;
import com.wallet.ledger.domain.valueobject.TransactionId;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable ledger entry. Double-entry: DEBIT and CREDIT. balance_after = snapshot for balance derivation.
 */
@Value
@Builder
public class LedgerEntry {

    EntryId entryId;
    TransactionId transactionId;
    AccountId accountId;
    EntryDirection direction;
    BigDecimal amount;
    BigDecimal balanceAfter;
    Instant createdAt;

    public boolean isDebit() {
        return direction == EntryDirection.DEBIT;
    }

    public boolean isCredit() {
        return direction == EntryDirection.CREDIT;
    }
}
