package com.wallet.ledger.application.service;

import com.wallet.ledger.application.port.FindAccountPort;
import com.wallet.ledger.application.port.FindLedgerEntriesByTransactionIdPort;
import com.wallet.ledger.application.port.FindTransactionsPort;
import com.wallet.ledger.application.port.FindWalletPort;
import com.wallet.ledger.domain.entity.LedgerEntry;
import com.wallet.ledger.domain.entity.Transaction;
import com.wallet.ledger.domain.valueobject.AccountId;
import com.wallet.ledger.domain.valueobject.AccountType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionHistoryService {

    private final FindWalletPort findWalletPort;
    private final FindAccountPort findAccountPort;
    private final FindTransactionsPort findTransactionsPort;
    private final FindLedgerEntriesByTransactionIdPort findLedgerEntriesByTransactionIdPort;

    /** Transaction history with both credit and debit legs: each transaction returns one record per ledger entry (DEBIT and CREDIT). */
    public List<TransactionWithEntryDetail> getTransactionHistoryWithDetails(String userId) {
        log.debug("Transaction history with details userId={}", userId);
        var wallet = findWalletPort.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for userId: " + userId));
        var account = findAccountPort.findByWalletIdAndType(wallet.getWalletId(), AccountType.USER_WALLET_ACCOUNT)
                .orElseThrow(() -> new IllegalArgumentException("Wallet account not found for userId: " + userId));
        AccountId accountId = account.getAccountId();
        List<Transaction> transactions = findTransactionsPort.findByAccountId(accountId);
        List<TransactionWithEntryDetail> result = new ArrayList<>();
        for (Transaction txn : transactions) {
            List<LedgerEntry> entries = findLedgerEntriesByTransactionIdPort.findByTransactionId(txn.getTransactionId());
            for (LedgerEntry entry : entries) {
                result.add(TransactionWithEntryDetail.of(txn, entry));
            }
        }
        log.trace("Transaction history with details completed userId={} count={} (both credit and debit legs)", userId, result.size());
        return result;
    }

    public List<Transaction> getTransactions(String userId) {
        log.debug("Transaction history userId={}", userId);
        var wallet = findWalletPort.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for userId: " + userId));
        var account = findAccountPort.findByWalletIdAndType(wallet.getWalletId(), AccountType.USER_WALLET_ACCOUNT)
                .orElseThrow(() -> new IllegalArgumentException("Wallet account not found for userId: " + userId));
        List<Transaction> list = findTransactionsPort.findByAccountId(account.getAccountId());
        log.trace("Transaction history completed userId={}", userId);
        return list;
    }
}
