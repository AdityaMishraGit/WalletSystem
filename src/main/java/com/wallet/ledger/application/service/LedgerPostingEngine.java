package com.wallet.ledger.application.service;

import com.wallet.ledger.application.port.*;
import com.wallet.ledger.domain.entity.LedgerEntry;
import com.wallet.ledger.domain.entity.Transaction;
import com.wallet.ledger.domain.exception.InsufficientBalanceException;
import com.wallet.ledger.domain.exception.InvalidPostingException;
import com.wallet.ledger.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ledger posting engine. Double-entry: debits = credits; computes balance_after; persists atomically.
 */
@Service
@RequiredArgsConstructor
public class LedgerPostingEngine {

    private static final Logger log = LoggerFactory.getLogger(LedgerPostingEngine.class);
    private static final int AMOUNT_SCALE = 4;

    @Value("${ledger.system-accounts.master}")
    private String systemMasterAccountIdValue;

    private final LoadAccountBalancesPort loadAccountBalancesPort;
    private final PersistPostingPort persistPostingPort;

    @Transactional
    public PostingResult post(PostingCommand command) {
        log.debug("Posting txnId={} type={} legs={}", command.getTransactionId().value(), command.getTransactionType(), command.getLegs().size());
        validateLegsPresent(command);
        validateDebitsEqualCredits(command);
        Set<AccountId> accountIds = command.getLegs().stream().map(PostingLeg::getAccountId).collect(Collectors.toSet());
        Map<AccountId, BigDecimal> balances = loadAccountBalancesPort.loadBalances(accountIds);
        return buildAndPersist(command, balances);
    }

    private PostingResult buildAndPersist(PostingCommand command, Map<AccountId, BigDecimal> balances) {
        Transaction txn = Transaction.builder()
                .transactionId(command.getTransactionId())
                .transactionType(command.getTransactionType())
                .status(TransactionStatus.COMPLETED)
                .referenceId(command.getReferenceId())
                .createdAt(Instant.now())
                .serviceBundleId(command.getServiceBundleId())
                .provisioningReference(command.getProvisioningReference())
                .build();
        List<LedgerEntry> entries = new ArrayList<>();
        Instant now = Instant.now();
        for (PostingLeg leg : command.getLegs()) {
            BigDecimal currentBalance = balances.getOrDefault(leg.getAccountId(), BigDecimal.ZERO).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
            BigDecimal amount = leg.getAmount().setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
            BigDecimal balanceAfter = leg.getDirection() == EntryDirection.DEBIT
                    ? currentBalance.subtract(amount)
                    : currentBalance.add(amount);
            boolean allowedNegative = leg.getAccountId().value().toString().equals(systemMasterAccountIdValue);
            if (balanceAfter.compareTo(BigDecimal.ZERO) < 0 && !allowedNegative) {
                throw new InsufficientBalanceException(
                        "Insufficient balance for account " + leg.getAccountId().value() + ": current=" + currentBalance + ", debit=" + amount);
            }
            entries.add(LedgerEntry.builder()
                    .entryId(EntryId.generate())
                    .transactionId(command.getTransactionId())
                    .accountId(leg.getAccountId())
                    .direction(leg.getDirection())
                    .amount(amount)
                    .balanceAfter(balanceAfter)
                    .createdAt(now)
                    .build());
        }
        persistPostingPort.persist(txn, entries);
        log.debug("Posted txnId={} entries={}", txn.getTransactionId().value(), entries.size());
        return PostingResult.of(txn);
    }

    private void validateLegsPresent(PostingCommand command) {
        if (command.getLegs() == null || command.getLegs().isEmpty()) {
            throw new InvalidPostingException("Posting must have at least one leg");
        }
    }

    private void validateDebitsEqualCredits(PostingCommand command) {
        BigDecimal totalDebits = command.getLegs().stream()
                .filter(l -> l.getDirection() == EntryDirection.DEBIT)
                .map(l -> l.getAmount().setScale(AMOUNT_SCALE, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredits = command.getLegs().stream()
                .filter(l -> l.getDirection() == EntryDirection.CREDIT)
                .map(l -> l.getAmount().setScale(AMOUNT_SCALE, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new InvalidPostingException("Debits (" + totalDebits + ") must equal credits (" + totalCredits + ")");
        }
    }
}
