package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.entity.Account;
import com.wallet.ledger.domain.valueobject.AccountId;

import java.util.Optional;

public interface LockAccountPort {
    Optional<Account> lockForUpdate(AccountId accountId);
}
