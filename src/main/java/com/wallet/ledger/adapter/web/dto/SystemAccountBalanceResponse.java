package com.wallet.ledger.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "System account balance")
public class SystemAccountBalanceResponse {

    @Schema(description = "Account type (e.g. SETTLEMENT_ACCOUNT)")
    private String accountType;

    @Schema(description = "Account UUID")
    private String accountId;

    @Schema(description = "Current balance from ledger")
    private BigDecimal balance;
}
