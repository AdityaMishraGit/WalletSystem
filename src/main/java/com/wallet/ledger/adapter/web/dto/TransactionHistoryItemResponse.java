package com.wallet.ledger.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transaction history item with userId, amount debited/credited, direction and balance after")
public class TransactionHistoryItemResponse {

    @Schema(description = "User ID (unique per wallet)")
    private String userId;

    @Schema(description = "Account ID for this leg (identifies which account was debited or credited)")
    private String accountId;

    @Schema(description = "Transaction UUID")
    private String transactionId;

    @Schema(description = "Transaction type (CASH_IN, CASH_OUT, TRANSFER, PROVISIONING, etc.)")
    private String transactionType;

    @Schema(description = "Transaction status")
    private String status;

    @Schema(description = "Reference ID")
    private String referenceId;

    @Schema(description = "When the transaction was created")
    private Instant createdAt;

    @Schema(description = "For PROVISIONING: service bundle ID")
    private String serviceBundleId;

    @Schema(description = "For PROVISIONING: e.g. phone number, subscription id")
    private String provisioningReference;

    @Schema(description = "Amount for this leg (debit or credit)")
    private BigDecimal amount;

    @Schema(description = "DEBIT = amount deducted from this account, CREDIT = amount added to this account")
    private String direction;

    @Schema(description = "Balance of this account after this transaction was applied")
    private BigDecimal balanceAfter;

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private String userId; private String accountId; private String transactionId; private String transactionType; private String status;
        private String referenceId; private Instant createdAt; private String serviceBundleId; private String provisioningReference;
        private BigDecimal amount; private String direction; private BigDecimal balanceAfter;
        public Builder userId(String v) { this.userId = v; return this; }
        public Builder accountId(String v) { this.accountId = v; return this; }
        public Builder transactionId(String v) { this.transactionId = v; return this; }
        public Builder transactionType(String v) { this.transactionType = v; return this; }
        public Builder status(String v) { this.status = v; return this; }
        public Builder referenceId(String v) { this.referenceId = v; return this; }
        public Builder createdAt(Instant v) { this.createdAt = v; return this; }
        public Builder serviceBundleId(String v) { this.serviceBundleId = v; return this; }
        public Builder provisioningReference(String v) { this.provisioningReference = v; return this; }
        public Builder amount(BigDecimal v) { this.amount = v; return this; }
        public Builder direction(String v) { this.direction = v; return this; }
        public Builder balanceAfter(BigDecimal v) { this.balanceAfter = v; return this; }
        public TransactionHistoryItemResponse build() {
            return new TransactionHistoryItemResponse(userId, accountId, transactionId, transactionType, status, referenceId,
                    createdAt, serviceBundleId, provisioningReference, amount, direction, balanceAfter);
        }
    }
}
