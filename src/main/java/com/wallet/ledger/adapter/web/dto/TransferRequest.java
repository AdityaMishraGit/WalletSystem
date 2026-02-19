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
@Schema(description = "P2P transfer request")
public class TransferRequest {

    @NotNull(message = "fromUserId is required")
    @Schema(description = "Sender user ID (unique per wallet)", required = true)
    private String fromUserId;

    @NotNull(message = "toUserId is required")
    @Schema(description = "Receiver user ID (unique per wallet)", required = true)
    private String toUserId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be positive")
    @Schema(description = "Amount to transfer", example = "25.00", required = true)
    private BigDecimal amount;
}
