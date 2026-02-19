package com.wallet.ledger.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Wallet resource")
public class WalletResponse {

    private String walletId;
    private String userId;
    private String status;
    private String currency;
    private Instant createdAt;
}
