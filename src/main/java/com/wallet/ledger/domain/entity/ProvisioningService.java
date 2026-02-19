package com.wallet.ledger.domain.entity;

import com.wallet.ledger.domain.valueobject.ProvisioningServiceType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class ProvisioningService {

    UUID id;
    ProvisioningServiceType type;
    String name;
    String code;
    String subcategory;
    String description;
    String status;
    Instant createdAt;

    public UUID getId() { return id; }
    public ProvisioningServiceType getType() { return type; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getSubcategory() { return subcategory; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
