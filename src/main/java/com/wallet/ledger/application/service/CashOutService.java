package com.wallet.ledger.application.service;

import com.wallet.ledger.application.port.*;
import com.wallet.ledger.domain.entity.Account;
import com.wallet.ledger.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashOutService {

    private final FindWalletPort findWalletPort;
    private final FindAccountPort findAccountPort;
    private final LockAccountPort lockAccountPort;
    private final LedgerPostingEngine ledgerPostingEngine;

    /** Step 1: DEBIT user_wallet, CREDIT withdrawal_pending */
    @Transactional
    public PostingResult reserveWithdrawal(String userId, BigDecimal amount, String referenceId) {
        log.debug("Reserve withdrawal userId={} amount={} referenceId={}", userId, amount, referenceId);
        var wallet = findWalletPort.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for userId: " + userId));
        Account userAccount = findAccountPort.findByWalletIdAndType(wallet.getWalletId(), AccountType.USER_WALLET_ACCOUNT)
                .orElseThrow(() -> new IllegalArgumentException("Wallet account not found for userId: " + userId));
        Account pendingAccount = findAccountPort.findSystemAccountByType(AccountType.WITHDRAWAL_PENDING_ACCOUNT)
                .orElseThrow(() -> new IllegalStateException("Withdrawal pending account not found"));
        lockAccountPort.lockForUpdate(userAccount.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + userAccount.getAccountId().value()));
        PostingCommand cmd = PostingCommand.builder()
                .transactionId(TransactionId.generate())
                .transactionType(TransactionType.WITHDRAWAL_RESERVE)
                .referenceId(referenceId)
                .legs(List.of(
                        PostingLeg.builder().accountId(userAccount.getAccountId()).direction(EntryDirection.DEBIT).amount(amount).build(),
                        PostingLeg.builder().accountId(pendingAccount.getAccountId()).direction(EntryDirection.CREDIT).amount(amount).build()))
                .build();
        PostingResult result = ledgerPostingEngine.post(cmd);
        log.info("Withdrawal reserved userId={} txnId={}", userId, result.getTransaction().getTransactionId().value());
        return result;
    }

    /** Step 2: DEBIT withdrawal_pending, CREDIT settlement_account */
    public PostingResult settleWithdrawal(String referenceId, BigDecimal amount) {
        log.debug("Settle withdrawal referenceId={} amount={}", referenceId, amount);
        Account pending = findAccountPort.findSystemAccountByType(AccountType.WITHDRAWAL_PENDING_ACCOUNT)
                .orElseThrow(() -> new IllegalStateException("Withdrawal pending account not found"));
        Account settlement = findAccountPort.findSystemAccountByType(AccountType.SETTLEMENT_ACCOUNT)
                .orElseThrow(() -> new IllegalStateException("Settlement account not found"));
        PostingCommand cmd = PostingCommand.builder()
                .transactionId(TransactionId.generate())
                .transactionType(TransactionType.WITHDRAWAL_SETTLE)
                .referenceId(referenceId)
                .legs(List.of(
                        PostingLeg.builder().accountId(pending.getAccountId()).direction(EntryDirection.DEBIT).amount(amount).build(),
                        PostingLeg.builder().accountId(settlement.getAccountId()).direction(EntryDirection.CREDIT).amount(amount).build()))
                .build();
        PostingResult result = ledgerPostingEngine.post(cmd);
        log.info("Withdrawal settled referenceId={} txnId={}", referenceId, result.getTransaction().getTransactionId().value());
        return result;
    }
}
