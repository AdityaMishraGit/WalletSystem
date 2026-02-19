package com.wallet.ledger.application.service;

import com.wallet.ledger.application.port.*;
import com.wallet.ledger.domain.entity.Account;
import com.wallet.ledger.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashInService {

    private final FindWalletPort findWalletPort;
    private final FindAccountPort findAccountPort;
    private final LedgerPostingEngine ledgerPostingEngine;

    public PostingResult cashIn(String userId, BigDecimal amount, String referenceId) {
        log.debug("Cash-in userId={} amount={} referenceId={}", userId, amount, referenceId);
        var wallet = findWalletPort.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for userId: " + userId));
        Account settlement = findAccountPort.findSystemAccountByType(AccountType.SETTLEMENT_ACCOUNT)
                .orElseThrow(() -> new IllegalStateException("Settlement account not found"));
        Account userAccount = findAccountPort.findByWalletIdAndType(wallet.getWalletId(), AccountType.USER_WALLET_ACCOUNT)
                .orElseThrow(() -> new IllegalArgumentException("User wallet account not found for userId: " + userId));
        PostingCommand cmd = PostingCommand.builder()
                .transactionId(TransactionId.generate())
                .transactionType(TransactionType.CASH_IN)
                .referenceId(referenceId)
                .legs(List.of(
                        PostingLeg.builder().accountId(settlement.getAccountId()).direction(EntryDirection.DEBIT).amount(amount).build(),
                        PostingLeg.builder().accountId(userAccount.getAccountId()).direction(EntryDirection.CREDIT).amount(amount).build()))
                .build();
        PostingResult result = ledgerPostingEngine.post(cmd);
        log.info("Cash-in completed userId={} txnId={}", userId, result.getTransaction().getTransactionId().value());
        return result;
    }
}
