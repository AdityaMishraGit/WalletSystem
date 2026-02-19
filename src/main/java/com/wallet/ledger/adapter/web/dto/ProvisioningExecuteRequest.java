package com.wallet.ledger.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to execute a provisioning transaction (pay for a service bundle from wallet)")
public class ProvisioningExecuteRequest {

    @NotNull(message = "userId is required")
    @Schema(description = "User ID (unique per wallet); primary key for transactional APIs", required = true)
    private String userId;

    @NotNull(message = "bundleId is required")
    @Schema(description = "Service bundle ID to provision", required = true, example = "650e8400-e29b-41d4-a716-446655440001")
    private String bundleId;

    @Schema(description = "Provisioning reference (e.g. phone number, subscription id, meter number)")
    private String provisioningReference;

    public String getUserId() { return userId; }
    public String getBundleId() { return bundleId; }
    public String getProvisioningReference() { return provisioningReference; }
}
