package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.entity.Transaction;

import java.util.Optional;

public interface FindTransactionByReferencePort {
    Optional<Transaction> findByReferenceId(String referenceId);
}
