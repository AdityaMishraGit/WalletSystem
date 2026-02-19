package com.wallet.ledger.domain.entity;

import com.wallet.ledger.domain.valueobject.TransactionId;
import com.wallet.ledger.domain.valueobject.TransactionStatus;
import com.wallet.ledger.domain.valueobject.TransactionType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Transaction entity. referenceId = idempotency key. Never delete; use reversal.
 */
@Value
@Builder
public class Transaction {

    TransactionId transactionId;
    TransactionType transactionType;
    TransactionStatus status;
    String referenceId;
    Instant createdAt;

    public boolean isCompleted() {
        return status == TransactionStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == TransactionStatus.FAILED;
    }

    public boolean isPending() {
        return status == TransactionStatus.PENDING;
    }
}
