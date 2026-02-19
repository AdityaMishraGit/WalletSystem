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
@Schema(description = "Reversal request")
public class ReversalRequest {

    @NotBlank(message = "originalReferenceId is required")
    @Schema(description = "Reference id of the transaction to reverse", required = true)
    private String originalReferenceId;
}
