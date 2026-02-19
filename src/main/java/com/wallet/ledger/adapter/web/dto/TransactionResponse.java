package com.wallet.ledger.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transaction record")
public class TransactionResponse {

    private String transactionId;
    private String transactionType;
    private String status;
    private String referenceId;
    private Instant createdAt;
    /** For PROVISIONING: bundle id */
    private String serviceBundleId;
    /** For PROVISIONING: e.g. phone number, subscription id */
    private String provisioningReference;

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private String transactionId; private String transactionType; private String status; private String referenceId;
        private Instant createdAt; private String serviceBundleId; private String provisioningReference;
        public Builder transactionId(String v) { this.transactionId = v; return this; }
        public Builder transactionType(String v) { this.transactionType = v; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder referenceId(String v) { this.referenceId = v; return this; }
        public Builder createdAt(Instant v) { this.createdAt = v; return this; }
        public Builder serviceBundleId(String v) { this.serviceBundleId = v; return this; }
        public Builder provisioningReference(String v) { this.provisioningReference = v; return this; }
        public TransactionResponse build() { return new TransactionResponse(transactionId, transactionType, status, referenceId, createdAt, serviceBundleId, provisioningReference); }
    }
}
