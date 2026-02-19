package com.wallet.ledger.adapter.persistence;

import com.wallet.ledger.application.port.*;
import com.wallet.ledger.domain.entity.LedgerEntry;
import com.wallet.ledger.domain.entity.Transaction;
import com.wallet.ledger.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LedgerPersistenceAdapter implements LoadAccountBalancesPort, PersistPostingPort, FindLedgerEntriesByTransactionIdPort {

    private static final String LATEST_BALANCE_SQL = """
            SELECT balance_after FROM ledger_entry
            WHERE account_id = ?
            ORDER BY created_at DESC
            LIMIT 1
            """;

    private static final RowMapper<LedgerEntry> LEDGER_ENTRY_ROW_MAPPER = (rs, rowNum) -> LedgerEntry.builder()
            .entryId(EntryId.of(UUID.fromString(rs.getString("entry_id"))))
            .transactionId(TransactionId.of(UUID.fromString(rs.getString("txn_id"))))
            .accountId(AccountId.of(UUID.fromString(rs.getString("account_id"))))
            .direction(EntryDirection.valueOf(rs.getString("direction")))
            .amount(rs.getBigDecimal("amount"))
            .balanceAfter(rs.getBigDecimal("balance_after"))
            .createdAt(toInstant(rs.getTimestamp("created_at")))
            .build();

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<AccountId, BigDecimal> loadBalances(Set<AccountId> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) return Map.of();
        Map<AccountId, BigDecimal> map = new HashMap<>();
        for (AccountId accountId : accountIds) {
            List<BigDecimal> list = jdbcTemplate.query(LATEST_BALANCE_SQL,
                    (rs, rowNum) -> rs.getBigDecimal("balance_after"), accountId.value());
            map.put(accountId, list.isEmpty() ? BigDecimal.ZERO : list.get(0));
        }
        return Map.copyOf(map);
    }

    @Override
    public void persist(Transaction transaction, List<LedgerEntry> entries) {
        log.trace("Persist txnId={} entries={}", transaction.getTransactionId().value(), entries.size());
        jdbcTemplate.update(
                "INSERT INTO transaction (txn_id, txn_type, status, reference_id, created_at) VALUES (?, ?, ?, ?, ?)",
                transaction.getTransactionId().value(), transaction.getTransactionType().name(),
                transaction.getStatus().name(), transaction.getReferenceId(),
                Timestamp.from(transaction.getCreatedAt()));
        String insertEntry = "INSERT INTO ledger_entry (entry_id, txn_id, account_id, direction, amount, balance_after, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        for (LedgerEntry entry : entries) {
            jdbcTemplate.update(insertEntry,
                    entry.getEntryId().value(), entry.getTransactionId().value(), entry.getAccountId().value(),
                    entry.getDirection().name(), entry.getAmount(), entry.getBalanceAfter(),
                    Timestamp.from(entry.getCreatedAt()));
        }
    }

    @Override
    public List<LedgerEntry> findByTransactionId(TransactionId transactionId) {
        return jdbcTemplate.query(
                "SELECT entry_id, txn_id, account_id, direction, amount, balance_after, created_at FROM ledger_entry WHERE txn_id = ? ORDER BY created_at",
                LEDGER_ENTRY_ROW_MAPPER, transactionId.value());
    }

    private static Instant toInstant(java.sql.Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }
}
