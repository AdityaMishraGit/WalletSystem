package com.wallet.ledger.application.service;

import com.wallet.ledger.application.port.FindAccountPort;
import com.wallet.ledger.application.port.FindWalletPort;
import com.wallet.ledger.application.port.LoadAccountBalancesPort;
import com.wallet.ledger.domain.valueobject.AccountType;
import com.wallet.ledger.domain.valueobject.WalletId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceService {

    private final FindWalletPort findWalletPort;
    private final FindAccountPort findAccountPort;
    private final LoadAccountBalancesPort loadAccountBalancesPort;

    public BigDecimal getBalance(WalletId walletId) {
        log.debug("Balance enquiry walletId={}", walletId.value());
        findWalletPort.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletId.value()));
        var account = findAccountPort.findByWalletIdAndType(walletId, AccountType.USER_WALLET_ACCOUNT)
                .orElseThrow(() -> new IllegalArgumentException("Wallet account not found: " + walletId.value()));
        var balances = loadAccountBalancesPort.loadBalances(Set.of(account.getAccountId()));
        BigDecimal balance = balances.getOrDefault(account.getAccountId(), BigDecimal.ZERO);
        log.trace("Balance walletId={} balance={}", walletId.value(), balance);
        return balance;
    }
}
