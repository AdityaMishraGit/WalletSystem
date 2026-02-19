package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.entity.Transaction;
import com.wallet.ledger.domain.valueobject.AccountId;

import java.util.List;

public interface FindTransactionsPort {
    List<Transaction> findByAccountId(AccountId accountId);
}
