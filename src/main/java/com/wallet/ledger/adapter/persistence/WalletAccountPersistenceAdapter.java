package com.wallet.ledger.adapter.persistence;

import com.wallet.ledger.application.port.*;
import com.wallet.ledger.domain.entity.Account;
import com.wallet.ledger.domain.entity.Transaction;
import com.wallet.ledger.domain.entity.Wallet;
import com.wallet.ledger.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WalletAccountPersistenceAdapter implements SaveWalletPort, SaveAccountPort, FindWalletPort, FindAccountPort,
        LockAccountPort, FindTransactionsPort, FindTransactionByReferencePort {

    private static final Logger log = LoggerFactory.getLogger(WalletAccountPersistenceAdapter.class);
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Wallet> WALLET_ROW_MAPPER = (rs, rowNum) -> Wallet.builder()
            .walletId(WalletId.of(UUID.fromString(rs.getString("wallet_id"))))
            .userId(rs.getString("user_id"))
            .status(WalletStatus.valueOf(rs.getString("status")))
            .currency(rs.getString("currency"))
            .createdAt(toInstant(rs.getTimestamp("created_at")))
            .build();

    private static final RowMapper<Account> ACCOUNT_ROW_MAPPER = (rs, rowNum) -> {
        String walletIdStr = rs.getString("wallet_id");
        return Account.builder()
                .accountId(AccountId.of(UUID.fromString(rs.getString("account_id"))))
                .accountType(AccountType.valueOf(rs.getString("account_type")))
                .walletId(walletIdStr != null ? WalletId.of(UUID.fromString(walletIdStr)) : null)
                .status(AccountStatus.valueOf(rs.getString("status")))
                .build();
    };

    private static final RowMapper<Transaction> TRANSACTION_ROW_MAPPER = (rs, rowNum) -> Transaction.builder()
            .transactionId(TransactionId.of(UUID.fromString(rs.getString("txn_id"))))
            .transactionType(TransactionType.valueOf(rs.getString("txn_type")))
            .status(TransactionStatus.valueOf(rs.getString("status")))
            .referenceId(rs.getString("reference_id"))
            .createdAt(toInstant(rs.getTimestamp("created_at")))
            .serviceBundleId(rs.getObject("service_bundle_id", UUID.class))
            .provisioningReference(rs.getString("provisioning_reference"))
            .build();

    @Override
    public Wallet save(Wallet wallet) {
        jdbcTemplate.update(
                "INSERT INTO wallet (wallet_id, user_id, status, currency, created_at) VALUES (?, ?, ?, ?, ?)",
                wallet.getWalletId().value(), wallet.getUserId(), wallet.getStatus().name(), wallet.getCurrency(),
                Timestamp.from(wallet.getCreatedAt()));
        log.debug("Saved wallet walletId={}", wallet.getWalletId().value());
        return wallet;
    }

    @Override
    public Account save(Account account) {
        jdbcTemplate.update(
                "INSERT INTO account (account_id, account_type, wallet_id, status) VALUES (?, ?, ?, ?)",
                account.getAccountId().value(), account.getAccountType().name(),
                account.getWalletId() != null ? account.getWalletId().value() : null,
                account.getStatus().name());
        log.debug("Saved account accountId={} type={}", account.getAccountId().value(), account.getAccountType());
        return account;
    }

    @Override
    public Optional<Wallet> findById(WalletId walletId) {
        List<Wallet> list = jdbcTemplate.query(
                "SELECT wallet_id, user_id, status, currency, created_at FROM wallet WHERE wallet_id = ?",
                WALLET_ROW_MAPPER, walletId.value());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<Wallet> findByUserId(String userId) {
        List<Wallet> list = jdbcTemplate.query(
                "SELECT wallet_id, user_id, status, currency, created_at FROM wallet WHERE user_id = ?",
                WALLET_ROW_MAPPER, userId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<Account> findByWalletIdAndType(WalletId walletId, AccountType accountType) {
        List<Account> list = jdbcTemplate.query(
                "SELECT account_id, account_type, wallet_id, status FROM account WHERE wallet_id = ? AND account_type = ?",
                ACCOUNT_ROW_MAPPER, walletId.value(), accountType.name());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<Account> findSystemAccountByType(AccountType accountType) {
        List<Account> list = jdbcTemplate.query(
                "SELECT account_id, account_type, wallet_id, status FROM account WHERE wallet_id IS NULL AND account_type = ?",
                ACCOUNT_ROW_MAPPER, accountType.name());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<Account> lockForUpdate(AccountId accountId) {
        List<Account> list = jdbcTemplate.query(
                "SELECT account_id, account_type, wallet_id, status FROM account WHERE account_id = ? FOR UPDATE",
                ACCOUNT_ROW_MAPPER, accountId.value());
        if (!list.isEmpty()) log.debug("Locked account accountId={}", accountId.value());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Transaction> findByAccountId(AccountId accountId) {
        return jdbcTemplate.query("""
                        SELECT DISTINCT t.txn_id, t.txn_type, t.status, t.reference_id, t.created_at, t.service_bundle_id, t.provisioning_reference
                        FROM transaction t INNER JOIN ledger_entry e ON e.txn_id = t.txn_id
                        WHERE e.account_id = ? ORDER BY t.created_at DESC
                        """,
                TRANSACTION_ROW_MAPPER, accountId.value());
    }

    @Override
    public Optional<Transaction> findByReferenceId(String referenceId) {
        List<Transaction> list = jdbcTemplate.query(
                "SELECT txn_id, txn_type, status, reference_id, created_at, service_bundle_id, provisioning_reference FROM transaction WHERE reference_id = ?",
                TRANSACTION_ROW_MAPPER, referenceId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private static Instant toInstant(java.sql.Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }
}
