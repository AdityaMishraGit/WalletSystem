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
public class TransferService {

    private final FindWalletPort findWalletPort;
    private final FindAccountPort findAccountPort;
    private final LockAccountPort lockAccountPort;
    private final LedgerPostingEngine ledgerPostingEngine;

    @Transactional
    public PostingResult transfer(String fromUserId, String toUserId, BigDecimal amount, String referenceId) {
        log.debug("Transfer fromUserId={} toUserId={} amount={} referenceId={}", fromUserId, toUserId, amount, referenceId);
        var fromWallet = findWalletPort.findByUserId(fromUserId)
                .orElseThrow(() -> new IllegalArgumentException("Sender wallet not found for userId: " + fromUserId));
        var toWallet = findWalletPort.findByUserId(toUserId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver wallet not found for userId: " + toUserId));
        Account fromAccount = findAccountPort.findByWalletIdAndType(fromWallet.getWalletId(), AccountType.USER_WALLET_ACCOUNT)
                .orElseThrow(() -> new IllegalArgumentException("Sender wallet account not found for userId: " + fromUserId));
        Account toAccount = findAccountPort.findByWalletIdAndType(toWallet.getWalletId(), AccountType.USER_WALLET_ACCOUNT)
                .orElseThrow(() -> new IllegalArgumentException("Receiver wallet account not found for userId: " + toUserId));
        lockAccountPort.lockForUpdate(fromAccount.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + fromAccount.getAccountId().value()));
        PostingCommand cmd = PostingCommand.builder()
                .transactionId(TransactionId.generate())
                .transactionType(TransactionType.TRANSFER)
                .referenceId(referenceId)
                .legs(List.of(
                        PostingLeg.builder().accountId(fromAccount.getAccountId()).direction(EntryDirection.DEBIT).amount(amount).build(),
                        PostingLeg.builder().accountId(toAccount.getAccountId()).direction(EntryDirection.CREDIT).amount(amount).build()))
                .build();
        PostingResult result = ledgerPostingEngine.post(cmd);
        log.info("Transfer completed fromUserId={} toUserId={} txnId={}", fromUserId, toUserId, result.getTransaction().getTransactionId().value());
        return result;
    }
}
