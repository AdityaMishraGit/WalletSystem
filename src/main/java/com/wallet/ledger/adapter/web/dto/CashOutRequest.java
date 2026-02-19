package com.wallet.ledger.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
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
@Schema(description = "Cash-out (reserve) request")
public class CashOutRequest {

    @NotNull(message = "walletId is required")
    @Schema(description = "Wallet UUID to debit", required = true)
    private String walletId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be positive")
    @Schema(description = "Amount to withdraw", example = "50.00", required = true)
    private BigDecimal amount;
}
