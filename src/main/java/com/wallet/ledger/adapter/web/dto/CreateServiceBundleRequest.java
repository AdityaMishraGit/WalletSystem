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
@Schema(description = "Request to create a service bundle under a provisioning service")
public class CreateServiceBundleRequest {

    @NotNull(message = "provisioningServiceId is required")
    @Schema(description = "ID of the provisioning service this bundle belongs to", required = true)
    private String provisioningServiceId;

    @NotBlank(message = "name is required")
    @Schema(description = "Bundle display name (e.g. DSTv Compact, Netflix Basic)", required = true)
    private String name;

    @NotBlank(message = "code is required")
    @Schema(description = "Bundle code (unique per service)", required = true)
    private String code;

    @NotNull(message = "fixedAmount is required")
    @DecimalMin(value = "0", inclusive = true, message = "fixedAmount must be >= 0")
    @Schema(description = "Fixed price/amount for this bundle", required = true)
    private BigDecimal fixedAmount;

    @Schema(description = "Subcategory (e.g. entertainment)")
    private String subcategory;

    @Schema(description = "Validity in days (e.g. 30 for monthly)")
    private Integer validityDays;

    @Schema(description = "Optional description")
    private String description;

    @Schema(description = "Status: ACTIVE or INACTIVE", example = "ACTIVE")
    private String status;
}
