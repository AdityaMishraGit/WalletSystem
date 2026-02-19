package com.wallet.ledger.application.service;

import com.wallet.ledger.application.port.FindAccountPort;
import com.wallet.ledger.application.port.FindTransactionsPort;
import com.wallet.ledger.application.port.FindWalletPort;
import com.wallet.ledger.domain.entity.Transaction;
import com.wallet.ledger.domain.valueobject.AccountType;
import com.wallet.ledger.domain.valueobject.WalletId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionHistoryService {

    private final FindWalletPort findWalletPort;
    private final FindAccountPort findAccountPort;
    private final FindTransactionsPort findTransactionsPort;

    public List<Transaction> getTransactions(WalletId walletId) {
        log.debug("Transaction history walletId={}", walletId.value());
        findWalletPort.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletId.value()));
        var account = findAccountPort.findByWalletIdAndType(walletId, AccountType.USER_WALLET_ACCOUNT)
                .orElseThrow(() -> new IllegalArgumentException("Wallet account not found: " + walletId.value()));
        List<Transaction> list = findTransactionsPort.findByAccountId(account.getAccountId());
        log.trace("Transaction history completed walletId={}", walletId.value());
        return list;
    }
}
