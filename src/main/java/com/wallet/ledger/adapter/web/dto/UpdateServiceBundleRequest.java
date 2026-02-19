package com.wallet.ledger.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a service bundle")
public class UpdateServiceBundleRequest {

    @NotNull(message = "provisioningServiceId is required")
    @Schema(description = "ID of the provisioning service this bundle belongs to", required = true)
    private String provisioningServiceId;

    @NotBlank(message = "name is required")
    @Schema(description = "Bundle display name", required = true)
    private String name;

    @NotBlank(message = "code is required")
    @Schema(description = "Bundle code", required = true)
    private String code;

    @NotNull(message = "fixedAmount is required")
    @DecimalMin(value = "0", inclusive = true, message = "fixedAmount must be >= 0")
    @Schema(description = "Fixed price/amount for this bundle", required = true)
    private BigDecimal fixedAmount;

    @Schema(description = "Subcategory")
    private String subcategory;

    @Schema(description = "Validity in days")
    private Integer validityDays;

    @Schema(description = "Description")
    private String description;

    @NotNull(message = "status is required")
    @Schema(description = "Status: ACTIVE or INACTIVE", required = true)
    private String status;
}
