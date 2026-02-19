package com.wallet.ledger.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Service bundle (e.g. DSTv Compact 50, Netflix Basic)")
public class ServiceBundleResponse {

    private String id;
    private String provisioningServiceId;
    private String name;
    private String code;
    private BigDecimal amountMin;
    private BigDecimal amountMax;
    private BigDecimal fixedAmount;
    private String subcategory;
    private Integer validityDays;
    private String description;
    private String status;
    private Instant createdAt;
    /** Effective amount to charge (fixedAmount or amountMin) */
    private BigDecimal effectiveAmount;

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private String id; private String provisioningServiceId; private String name; private String code;
        private BigDecimal amountMin; private BigDecimal amountMax; private BigDecimal fixedAmount;
        private String subcategory; private Integer validityDays; private String description; private String status;
        private Instant createdAt; private BigDecimal effectiveAmount;
        public Builder id(String id) { this.id = id; return this; }
        public Builder provisioningServiceId(String v) { this.provisioningServiceId = v; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder code(String code) { this.code = code; return this; }
        public Builder amountMin(BigDecimal v) { this.amountMin = v; return this; }
        public Builder amountMax(BigDecimal v) { this.amountMax = v; return this; }
        public Builder fixedAmount(BigDecimal v) { this.fixedAmount = v; return this; }
        public Builder subcategory(String v) { this.subcategory = v; return this; }
        public Builder validityDays(Integer v) { this.validityDays = v; return this; }
        public Builder description(String v) { this.description = v; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder createdAt(Instant v) { this.createdAt = v; return this; }
        public Builder effectiveAmount(BigDecimal v) { this.effectiveAmount = v; return this; }
        public ServiceBundleResponse build() { return new ServiceBundleResponse(id, provisioningServiceId, name, code, amountMin, amountMax, fixedAmount, subcategory, validityDays, description, status, createdAt, effectiveAmount); }
    }
}
