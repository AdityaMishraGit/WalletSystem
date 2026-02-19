package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.entity.LedgerEntry;
import com.wallet.ledger.domain.valueobject.TransactionId;

import java.util.List;

/** Port: find all ledger entries for a transaction (for reversal). */
public interface FindLedgerEntriesByTransactionIdPort {

    List<LedgerEntry> findByTransactionId(TransactionId transactionId);
}
