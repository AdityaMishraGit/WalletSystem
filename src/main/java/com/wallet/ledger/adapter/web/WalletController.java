package com.wallet.ledger.adapter.web;

import com.wallet.ledger.adapter.web.dto.*;
import com.wallet.ledger.application.port.FindWalletPort;
import com.wallet.ledger.application.service.*;
import com.wallet.ledger.application.service.TransactionWithEntryDetail;
import com.wallet.ledger.domain.entity.Transaction;
import com.wallet.ledger.domain.entity.Wallet;
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
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Wallet Ledger", description = "Wallet onboarding, cash-in/out, transfer, balance, history, reversal")
public class WalletController {

    private final FindWalletPort findWalletPort;
    private final CreateWalletService createWalletService;
    private final CashInService cashInService;
    private final CashOutService cashOutService;
    private final TransferService transferService;
    private final BalanceService balanceService;
    private final TransactionHistoryService transactionHistoryService;
    private final ReversalService reversalService;

    @PostMapping(value = "/wallets", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create wallet", description = "User onboarding: create wallet and USER_WALLET_ACCOUNT")
    @ApiResponse(responseCode = "200", description = "Wallet created", content = @Content(schema = @Schema(implementation = WalletResponse.class)))
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        log.info("POST /wallets userId={}", request.getUserId());
        Wallet wallet = createWalletService.createWallet(request.getUserId(), request.getCurrency());
        WalletResponse body = toWalletResponse(wallet);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @PostMapping(value = "/cashin", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cash-in", description = "DEBIT settlement_account, CREDIT user_wallet. Uses userId (unique per wallet).")
    @ApiResponse(responseCode = "200", description = "Cash-in completed", content = @Content(schema = @Schema(implementation = TransactionResponse.class)))
    public ResponseEntity<TransactionResponse> cashIn(@Valid @RequestBody CashInRequest request) {
        log.info("POST /cashin userId={} amount={}", request.getUserId(), request.getAmount());
        String ref = UUID.randomUUID().toString();
        var result = cashInService.cashIn(request.getUserId(), request.getAmount(), ref);
        TransactionResponse body = toTransactionResponse(result.getTransaction());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @PostMapping(value = "/cashout", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cash-out (reserve)", description = "Step 1: DEBIT user_wallet, CREDIT withdrawal_pending. Uses userId.")
    @ApiResponse(responseCode = "200", description = "Withdrawal reserved", content = @Content(schema = @Schema(implementation = TransactionResponse.class)))
    public ResponseEntity<TransactionResponse> cashOut(@Valid @RequestBody CashOutRequest request) {
        log.info("POST /cashout userId={} amount={}", request.getUserId(), request.getAmount());
        String ref = UUID.randomUUID().toString();
        var result = cashOutService.reserveWithdrawal(request.getUserId(), request.getAmount(), ref);
        TransactionResponse body = toTransactionResponse(result.getTransaction());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @PostMapping(value = "/transfer", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "P2P transfer", description = "Locks sender; DEBIT sender, CREDIT receiver. Uses fromUserId and toUserId.")
    @ApiResponse(responseCode = "200", description = "Transfer completed", content = @Content(schema = @Schema(implementation = TransactionResponse.class)))
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("POST /transfer fromUserId={} toUserId={} amount={}", request.getFromUserId(), request.getToUserId(), request.getAmount());
        String ref = UUID.randomUUID().toString();
        var result = transferService.transfer(
                request.getFromUserId(),
                request.getToUserId(),
                request.getAmount(),
                ref);
        TransactionResponse body = toTransactionResponse(result.getTransaction());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @GetMapping(value = "/balance/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get balance", description = "Balance from latest ledger_entry.balance_after. userId is unique per wallet.")
    @ApiResponse(responseCode = "200", description = "Wallet balance", content = @Content(schema = @Schema(implementation = BalanceResponse.class)))
    public ResponseEntity<BalanceResponse> getBalance(
            @Parameter(description = "User ID (unique per wallet)") @PathVariable String userId) {
        log.info("GET /balance/{}", userId);
        var balance = balanceService.getBalance(userId);
        var wallet = findWalletPort.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Wallet not found for userId: " + userId));
        BalanceResponse body = BalanceResponse.builder()
                .userId(userId)
                .walletId(wallet.getWalletId().value().toString())
                .balance(balance)
                .build();
        return ResponseEntity.ok().body(body);
    }

    @GetMapping(value = "/transactions/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Transaction history", description = "Transactions for this user's wallet with userId, amount debited/credited, direction (DEBIT/CREDIT), balanceAfter and full transaction details. Newest first.")
    @ApiResponse(responseCode = "200", description = "List of transaction history items with amount and direction", content = @Content(schema = @Schema(implementation = TransactionHistoryItemResponse.class)))
    public ResponseEntity<List<TransactionHistoryItemResponse>> getTransactions(
            @Parameter(description = "User ID (unique per wallet)") @PathVariable String userId) {
        log.info("GET /transactions/{}", userId);
        List<TransactionWithEntryDetail> details = transactionHistoryService.getTransactionHistoryWithDetails(userId);
        List<TransactionHistoryItemResponse> body = details.stream()
                .map(d -> toTransactionHistoryItemResponse(userId, d))
                .toList();
        return ResponseEntity.ok().body(body);
    }

    @PostMapping(value = "/reversal", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Reverse transaction", description = "Create compensating REVERSAL transaction")
    @ApiResponse(responseCode = "200", description = "Reversal completed", content = @Content(schema = @Schema(implementation = TransactionResponse.class)))
    public ResponseEntity<TransactionResponse> reversal(@Valid @RequestBody ReversalRequest request) {
        log.info("POST /reversal originalRef={}", request.getOriginalReferenceId());
        String ref = UUID.randomUUID().toString();
        var result = reversalService.reverse(request.getOriginalReferenceId(), ref);
        TransactionResponse body = toTransactionResponse(result.getTransaction());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    private WalletResponse toWalletResponse(Wallet w) {
        return WalletResponse.builder()
                .walletId(w.getWalletId().value().toString())
                .userId(w.getUserId())
                .status(w.getStatus().name())
                .currency(w.getCurrency())
                .createdAt(w.getCreatedAt())
                .build();
    }

    private TransactionResponse toTransactionResponse(Transaction t) {
        return TransactionResponse.builder()
                .transactionId(t.getTransactionId().value().toString())
                .transactionType(t.getTransactionType().name())
                .status(t.getStatus().name())
                .referenceId(t.getReferenceId())
                .createdAt(t.getCreatedAt())
                .serviceBundleId(t.getServiceBundleId() != null ? t.getServiceBundleId().toString() : null)
                .provisioningReference(t.getProvisioningReference())
                .build();
    }

    private TransactionHistoryItemResponse toTransactionHistoryItemResponse(String userId, TransactionWithEntryDetail d) {
        Transaction t = d.transaction();
        return TransactionHistoryItemResponse.builder()
                .userId(userId)
                .accountId(d.accountId().value().toString())
                .transactionId(t.getTransactionId().value().toString())
                .transactionType(t.getTransactionType().name())
                .status(t.getStatus().name())
                .referenceId(t.getReferenceId())
                .createdAt(t.getCreatedAt())
                .serviceBundleId(t.getServiceBundleId() != null ? t.getServiceBundleId().toString() : null)
                .provisioningReference(t.getProvisioningReference())
                .amount(d.amount())
                .direction(d.direction().name())
                .balanceAfter(d.balanceAfter())
                .build();
    }
}
