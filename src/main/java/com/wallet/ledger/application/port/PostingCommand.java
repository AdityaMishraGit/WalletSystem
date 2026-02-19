package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.valueobject.TransactionId;
import com.wallet.ledger.domain.valueobject.TransactionType;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

/** Command for the ledger posting engine. Double-entry: sum of debits must equal sum of credits. */
@Value
@Builder
public class PostingCommand {

    TransactionId transactionId;
    TransactionType transactionType;
    String referenceId;
    List<PostingLeg> legs;
    /** For PROVISIONING: link to service_bundle.id */
    UUID serviceBundleId;
    /** For PROVISIONING: e.g. phone number, subscription id */
    String provisioningReference;

    public TransactionId getTransactionId() { return transactionId; }
    public TransactionType getTransactionType() { return transactionType; }
    public String getReferenceId() { return referenceId; }
    public List<PostingLeg> getLegs() { return legs; }
    public UUID getServiceBundleId() { return serviceBundleId; }
    public String getProvisioningReference() { return provisioningReference; }
}
