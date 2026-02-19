package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.entity.LedgerEntry;
import com.wallet.ledger.domain.entity.Transaction;

import java.util.List;

/** Port: persist a transaction and its ledger entries atomically. */
public interface PersistPostingPort {

    void persist(Transaction transaction, List<LedgerEntry> entries);
}
