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
@Schema(description = "Request to add amount to a system account")
public class SystemAccountCreditRequest {

    @NotNull(message = "accountType is required")
    @Schema(description = "System account type: SETTLEMENT_ACCOUNT, WITHDRAWAL_PENDING_ACCOUNT, FEE_ACCOUNT, REVERSAL_ACCOUNT", required = true, example = "SETTLEMENT_ACCOUNT")
    private String accountType;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be positive")
    @Schema(description = "Amount to credit to the system account", example = "500.00", required = true)
    private BigDecimal amount;
}
