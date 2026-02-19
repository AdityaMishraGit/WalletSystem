package com.wallet.ledger.domain.entity;

import com.wallet.ledger.domain.valueobject.TransactionId;
import com.wallet.ledger.domain.valueobject.TransactionStatus;
import com.wallet.ledger.domain.valueobject.TransactionType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Transaction entity. referenceId = idempotency key. Never delete; use reversal.
 * For PROVISIONING type: serviceBundleId and provisioningReference are set.
 */
@Value
@Builder
public class Transaction {

    TransactionId transactionId;
    TransactionType transactionType;
    TransactionStatus status;
    String referenceId;
    Instant createdAt;
    /** Set when txn_type = PROVISIONING; links to service_bundle.id */
    UUID serviceBundleId;
    /** Set when txn_type = PROVISIONING; e.g. phone number, subscription id */
    String provisioningReference;

    public TransactionId getTransactionId() { return transactionId; }
    public TransactionType getTransactionType() { return transactionType; }
    public TransactionStatus getStatus() { return status; }
    public String getReferenceId() { return referenceId; }
    public Instant getCreatedAt() { return createdAt; }
    public UUID getServiceBundleId() { return serviceBundleId; }
    public String getProvisioningReference() { return provisioningReference; }

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
