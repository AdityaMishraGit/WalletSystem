package com.wallet.ledger.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new wallet")
public class CreateWalletRequest {

    @NotBlank(message = "userId is required")
    @Schema(description = "External user identifier", example = "user-123", required = true)
    private String userId;

    @Schema(description = "Currency code", example = "USD", defaultValue = "USD")
    private String currency;
}
