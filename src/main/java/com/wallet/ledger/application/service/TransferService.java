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

    private final FindAccountPort findAccountPort;
    private final LockAccountPort lockAccountPort;
    private final LedgerPostingEngine ledgerPostingEngine;

    @Transactional
    public PostingResult transfer(WalletId fromWalletId, WalletId toWalletId, BigDecimal amount, String referenceId) {
        log.debug("Transfer from={} to={} amount={} referenceId={}", fromWalletId.value(), toWalletId.value(), amount, referenceId);
        Account fromAccount = findAccountPort.findByWalletIdAndType(fromWalletId, AccountType.USER_WALLET_ACCOUNT)
                .orElseThrow(() -> new IllegalArgumentException("Sender wallet account not found: " + fromWalletId.value()));
        Account toAccount = findAccountPort.findByWalletIdAndType(toWalletId, AccountType.USER_WALLET_ACCOUNT)
                .orElseThrow(() -> new IllegalArgumentException("Receiver wallet account not found: " + toWalletId.value()));
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
        log.info("Transfer completed from={} to={} txnId={}", fromWalletId.value(), toWalletId.value(), result.getTransaction().getTransactionId().value());
        return result;
    }
}
