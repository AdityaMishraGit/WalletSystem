package com.wallet.ledger.application.service;

import com.wallet.ledger.application.port.*;
import com.wallet.ledger.domain.entity.Account;
import com.wallet.ledger.domain.entity.Wallet;
import com.wallet.ledger.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateWalletService {

    /** Default amount credited to a new wallet when it is created. */
    public static final BigDecimal DEFAULT_WALLET_AMOUNT = new BigDecimal("100000");

    private final SaveWalletPort saveWalletPort;
    private final SaveAccountPort saveAccountPort;
    private final FindAccountPort findAccountPort;
    private final LedgerPostingEngine ledgerPostingEngine;

    @Transactional
    public Wallet createWallet(String userId, String currency) {
        WalletId walletId = WalletId.generate();
        AccountId accountId = AccountId.generate();
        Wallet wallet = Wallet.builder()
                .walletId(walletId)
                .userId(userId)
                .status(WalletStatus.ACTIVE)
                .currency(currency != null && !currency.isBlank() ? currency : "USD")
                .createdAt(Instant.now())
                .build();
        Account account = Account.builder()
                .accountId(accountId)
                .accountType(AccountType.USER_WALLET_ACCOUNT)
                .status(AccountStatus.ACTIVE)
                .walletId(walletId)
                .build();
        saveWalletPort.save(wallet);
        saveAccountPort.save(account);

        // Credit new wallet with default amount (DEBIT settlement, CREDIT user_wallet)
        Account settlement = findAccountPort.findSystemAccountByType(AccountType.SETTLEMENT_ACCOUNT)
                .orElseThrow(() -> new IllegalStateException("Settlement account not found"));
        String ref = "wallet-creation-" + walletId.value();
        PostingCommand cmd = PostingCommand.builder()
                .transactionId(TransactionId.generate())
                .transactionType(TransactionType.CASH_IN)
                .referenceId(ref)
                .legs(List.of(
                        PostingLeg.builder().accountId(settlement.getAccountId()).direction(EntryDirection.DEBIT).amount(DEFAULT_WALLET_AMOUNT).build(),
                        PostingLeg.builder().accountId(account.getAccountId()).direction(EntryDirection.CREDIT).amount(DEFAULT_WALLET_AMOUNT).build()))
                .build();
        ledgerPostingEngine.post(cmd);

        log.info("Created wallet walletId={} userId={} with default balance {}", wallet.getWalletId().value(), wallet.getUserId(), DEFAULT_WALLET_AMOUNT);
        return wallet;
    }
}
