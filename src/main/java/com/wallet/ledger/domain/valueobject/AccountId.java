package com.wallet.ledger.domain.valueobject;

import java.util.UUID;

public record AccountId(UUID value) {

    public static AccountId of(UUID uuid) {
        return new AccountId(uuid);
    }

    public static AccountId generate() {
        return new AccountId(UUID.randomUUID());
    }

    public static AccountId fromString(String s) {
        return new AccountId(UUID.fromString(s));
    }
}
