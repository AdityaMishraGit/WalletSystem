package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.valueobject.AccountId;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/** Port: load current balance (from latest ledger_entry.balance_after) for given accounts. */
public interface LoadAccountBalancesPort {

    Map<AccountId, BigDecimal> loadBalances(Set<AccountId> accountIds);
}
