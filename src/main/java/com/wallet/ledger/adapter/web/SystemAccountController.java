package com.wallet.ledger.adapter.web;

import com.wallet.ledger.adapter.web.dto.SystemAccountBalanceResponse;
import com.wallet.ledger.adapter.web.dto.SystemAccountCreditRequest;
import com.wallet.ledger.adapter.web.dto.TransactionResponse;
import com.wallet.ledger.application.service.SystemAccountService;
import com.wallet.ledger.domain.valueobject.AccountType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/system-accounts")
@RequiredArgsConstructor
@Tag(name = "System Accounts", description = "Balance and credit for system accounts (settlement, withdrawal-pending, fee, reversal)")
public class SystemAccountController {

    private final SystemAccountService systemAccountService;

    @GetMapping(value = "/balance", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List all system account balances", description = "Returns balance for each system account (SYSTEM_MASTER, SETTLEMENT, WITHDRAWAL_PENDING, FEE, REVERSAL)")
    @ApiResponse(responseCode = "200", description = "List of system account balances")
    public ResponseEntity<List<SystemAccountBalanceResponse>> getAllBalances() {
        log.info("GET /system-accounts/balance");
        var list = systemAccountService.getAllBalances().stream()
                .map(b -> SystemAccountBalanceResponse.builder()
                        .accountType(b.accountType())
                        .accountId(b.accountId())
                        .balance(b.balance())
                        .build())
                .toList();
        return ResponseEntity.ok().body(list);
    }

    @GetMapping(value = "/balance/{accountType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get balance for one system account", description = "Returns balance for the given system account type")
    @ApiResponse(responseCode = "200", description = "System account balance", content = @Content(schema = @Schema(implementation = SystemAccountBalanceResponse.class)))
    public ResponseEntity<SystemAccountBalanceResponse> getBalance(
            @Parameter(description = "Account type: SYSTEM_MASTER_ACCOUNT, SETTLEMENT_ACCOUNT, WITHDRAWAL_PENDING_ACCOUNT, FEE_ACCOUNT, REVERSAL_ACCOUNT")
            @PathVariable String accountType) {
        log.info("GET /system-accounts/balance/{}", accountType);
        AccountType type = AccountType.valueOf(accountType);
        var detail = systemAccountService.getBalanceDetail(type);
        SystemAccountBalanceResponse body = SystemAccountBalanceResponse.builder()
                .accountType(detail.accountType())
                .accountId(detail.accountId())
                .balance(detail.balance())
                .build();
        return ResponseEntity.ok().body(body);
    }

    @PostMapping(value = "/credit", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add amount to a system account", description = "Credits the given system account (DEBIT SYSTEM_MASTER, CREDIT target). Use for SETTLEMENT_ACCOUNT, WITHDRAWAL_PENDING_ACCOUNT, FEE_ACCOUNT, REVERSAL_ACCOUNT.")
    @ApiResponse(responseCode = "200", description = "Transaction created", content = @Content(schema = @Schema(implementation = TransactionResponse.class)))
    public ResponseEntity<TransactionResponse> credit(
            @Valid @RequestBody SystemAccountCreditRequest request) {
        log.info("POST /system-accounts/credit accountType={} amount={}", request.getAccountType(), request.getAmount());
        AccountType type = AccountType.valueOf(request.getAccountType());
        var result = systemAccountService.creditSystemAccount(type, request.getAmount());
        TransactionResponse body = TransactionResponse.builder()
                .transactionId(result.getTransaction().getTransactionId().value().toString())
                .transactionType(result.getTransaction().getTransactionType().name())
                .status(result.getTransaction().getStatus().name())
                .referenceId(result.getTransaction().getReferenceId())
                .createdAt(result.getTransaction().getCreatedAt())
                .build();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
