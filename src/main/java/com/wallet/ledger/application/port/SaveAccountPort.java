package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.entity.Account;

public interface SaveAccountPort {
    Account save(Account account);
}
