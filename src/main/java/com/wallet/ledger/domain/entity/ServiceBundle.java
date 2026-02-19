package com.wallet.ledger.domain.entity;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class ServiceBundle {

    UUID id;
    UUID provisioningServiceId;
    String name;
    String code;
    BigDecimal amountMin;   // null if fixed_amount is set
    BigDecimal amountMax;   // null if fixed_amount is set
    BigDecimal fixedAmount; // if set, use this as the transaction amount
    String subcategory;
    Integer validityDays;
    String description;
    String status;
    Instant createdAt;

    public UUID getId() { return id; }
    public UUID getProvisioningServiceId() { return provisioningServiceId; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public BigDecimal getAmountMin() { return amountMin; }
    public BigDecimal getAmountMax() { return amountMax; }
    public BigDecimal getFixedAmount() { return fixedAmount; }
    public String getSubcategory() { return subcategory; }
    public Integer getValidityDays() { return validityDays; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    /**
     * Effective amount to charge: fixed_amount if set, else amount_min (for range bundles use min as default or require client to send amount).
     */
    public BigDecimal getEffectiveAmount() {
        return fixedAmount != null ? fixedAmount : (amountMin != null ? amountMin : BigDecimal.ZERO);
    }
}
