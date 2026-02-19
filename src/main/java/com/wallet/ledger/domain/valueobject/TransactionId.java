package com.wallet.ledger.domain.valueobject;

import java.util.UUID;

public record TransactionId(UUID value) {

    public static TransactionId of(UUID uuid) {
        return new TransactionId(uuid);
    }

    public static TransactionId generate() {
        return new TransactionId(UUID.randomUUID());
    }

    public static TransactionId fromString(String s) {
        return new TransactionId(UUID.fromString(s));
    }
}
