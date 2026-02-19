package com.wallet.ledger.application.service;

import com.wallet.ledger.application.port.SaveAccountPort;
import com.wallet.ledger.application.port.SaveWalletPort;
import com.wallet.ledger.domain.entity.Account;
import com.wallet.ledger.domain.entity.Wallet;
import com.wallet.ledger.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateWalletService {

    private final SaveWalletPort saveWalletPort;
    private final SaveAccountPort saveAccountPort;

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
        log.info("Created wallet walletId={} userId={}", wallet.getWalletId().value(), wallet.getUserId());
        return wallet;
    }
}
