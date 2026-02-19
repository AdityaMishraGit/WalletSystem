package com.wallet.ledger.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a provisioning service")
public class CreateProvisioningServiceRequest {

    @NotNull(message = "type is required")
    @Schema(description = "Service type: TV_RECHARGE, PREPAID_RECHARGE, POSTPAID_RECHARGE, SUBSCRIPTION, DATA_BUNDLE, UTILITY_BILL", required = true)
    private String type;

    @NotBlank(message = "name is required")
    @Schema(description = "Display name (e.g. DSTv, Airtel Prepaid)", required = true)
    private String name;

    @NotBlank(message = "code is required")
    @Schema(description = "Unique service code", required = true)
    private String code;

    @Schema(description = "Subcategory (e.g. entertainment, mobile)")
    private String subcategory;

    @Schema(description = "Optional description")
    private String description;

    @Schema(description = "Status: ACTIVE or INACTIVE", example = "ACTIVE")
    private String status;
}
