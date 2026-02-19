package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.entity.Account;
import com.wallet.ledger.domain.valueobject.AccountType;
import com.wallet.ledger.domain.valueobject.WalletId;

import java.util.Optional;

public interface FindAccountPort {

    Optional<Account> findByWalletIdAndType(WalletId walletId, AccountType accountType);

    Optional<Account> findSystemAccountByType(AccountType accountType);
}
