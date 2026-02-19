package com.wallet.ledger.application.service;

import com.wallet.ledger.application.port.*;
import com.wallet.ledger.domain.entity.Account;
import com.wallet.ledger.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemAccountService {

    private static final Set<AccountType> SYSTEM_ACCOUNT_TYPES = Set.of(
            AccountType.SYSTEM_MASTER_ACCOUNT,
            AccountType.SETTLEMENT_ACCOUNT,
            AccountType.WITHDRAWAL_PENDING_ACCOUNT,
            AccountType.FEE_ACCOUNT,
            AccountType.REVERSAL_ACCOUNT
    );

    private final FindAccountPort findAccountPort;
    private final LoadAccountBalancesPort loadAccountBalancesPort;
    private final LedgerPostingEngine ledgerPostingEngine;

    /**
     * Get balance for one system account by type.
     */
    public BigDecimal getBalance(AccountType accountType) {
        return getBalanceDetail(accountType).balance();
    }

    /**
     * Get balance and account info for one system account by type.
     */
    public SystemAccountBalance getBalanceDetail(AccountType accountType) {
        if (!SYSTEM_ACCOUNT_TYPES.contains(accountType)) {
            throw new IllegalArgumentException("Not a system account type: " + accountType);
        }
        Account account = findAccountPort.findSystemAccountByType(accountType)
                .orElseThrow(() -> new IllegalArgumentException("System account not found: " + accountType));
        var balances = loadAccountBalancesPort.loadBalances(Set.of(account.getAccountId()));
        BigDecimal balance = balances.getOrDefault(account.getAccountId(), BigDecimal.ZERO);
        return new SystemAccountBalance(account.getAccountType().name(), account.getAccountId().value().toString(), balance);
    }

    /**
     * Get balances for all system accounts.
     */
    public List<SystemAccountBalance> getAllBalances() {
        List<SystemAccountBalance> result = new ArrayList<>();
        List<Account> accounts = new ArrayList<>();
        for (AccountType type : SYSTEM_ACCOUNT_TYPES) {
            findAccountPort.findSystemAccountByType(type).ifPresent(accounts::add);
        }
        if (accounts.isEmpty()) return result;
        Set<AccountId> ids = accounts.stream().map(Account::getAccountId).collect(Collectors.toSet());
        var balances = loadAccountBalancesPort.loadBalances(ids);
        for (Account acc : accounts) {
            result.add(new SystemAccountBalance(acc.getAccountType().name(), acc.getAccountId().value().toString(),
                    balances.getOrDefault(acc.getAccountId(), BigDecimal.ZERO)));
        }
        return result;
    }

    /**
     * Add amount to a system account (DEBIT SYSTEM_MASTER_ACCOUNT, CREDIT target). Cannot credit SYSTEM_MASTER_ACCOUNT.
     */
    @Transactional
    public PostingResult creditSystemAccount(AccountType accountType, BigDecimal amount) {
        if (accountType == AccountType.SYSTEM_MASTER_ACCOUNT) {
            throw new IllegalArgumentException("Cannot credit SYSTEM_MASTER_ACCOUNT via this API");
        }
        if (!SYSTEM_ACCOUNT_TYPES.contains(accountType)) {
            throw new IllegalArgumentException("Not a system account type: " + accountType);
        }
        Account master = findAccountPort.findSystemAccountByType(AccountType.SYSTEM_MASTER_ACCOUNT)
                .orElseThrow(() -> new IllegalStateException("SYSTEM_MASTER_ACCOUNT not found"));
        Account target = findAccountPort.findSystemAccountByType(accountType)
                .orElseThrow(() -> new IllegalArgumentException("System account not found: " + accountType));
        String ref = "system-credit-" + java.util.UUID.randomUUID();
        PostingCommand cmd = PostingCommand.builder()
                .transactionId(TransactionId.generate())
                .transactionType(TransactionType.SYSTEM_CREDIT)
                .referenceId(ref)
                .legs(List.of(
                        PostingLeg.builder().accountId(master.getAccountId()).direction(EntryDirection.DEBIT).amount(amount).build(),
                        PostingLeg.builder().accountId(target.getAccountId()).direction(EntryDirection.CREDIT).amount(amount).build()))
                .build();
        PostingResult result = ledgerPostingEngine.post(cmd);
        log.info("System account credited accountType={} amount={} txnId={}", accountType, amount, result.getTransaction().getTransactionId().value());
        return result;
    }

    public record SystemAccountBalance(String accountType, String accountId, BigDecimal balance) {}
}
