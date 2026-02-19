package com.wallet.ledger.application.service;

import com.wallet.ledger.application.port.FindAccountPort;
import com.wallet.ledger.application.port.FindWalletPort;
import com.wallet.ledger.application.port.LoadAccountBalancesPort;
import com.wallet.ledger.domain.valueobject.AccountType;
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

    public BigDecimal getBalance(String userId) {
        log.debug("Balance enquiry userId={}", userId);
        var wallet = findWalletPort.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for userId: " + userId));
        var account = findAccountPort.findByWalletIdAndType(wallet.getWalletId(), AccountType.USER_WALLET_ACCOUNT)
                .orElseThrow(() -> new IllegalArgumentException("Wallet account not found for userId: " + userId));
        var balances = loadAccountBalancesPort.loadBalances(Set.of(account.getAccountId()));
        BigDecimal balance = balances.getOrDefault(account.getAccountId(), BigDecimal.ZERO);
        log.trace("Balance userId={} balance={}", userId, balance);
        return balance;
    }
}
