package com.wallet.ledger.application.service;

import com.wallet.ledger.domain.entity.LedgerEntry;
import com.wallet.ledger.domain.entity.Transaction;
import com.wallet.ledger.domain.valueobject.AccountId;
import com.wallet.ledger.domain.valueobject.EntryDirection;

import java.math.BigDecimal;

/** One leg of a transaction: transaction plus one ledger entry (credit or debit). */
public record TransactionWithEntryDetail(
    Transaction transaction,
    AccountId accountId,
    BigDecimal amount,
    EntryDirection direction,
    BigDecimal balanceAfter
) {
    public static TransactionWithEntryDetail of(Transaction transaction, LedgerEntry entry) {
        return new TransactionWithEntryDetail(
            transaction,
            entry.getAccountId(),
            entry.getAmount(),
            entry.getDirection(),
            entry.getBalanceAfter()
        );
    }
}
