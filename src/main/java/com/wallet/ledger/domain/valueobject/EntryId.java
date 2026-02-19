package com.wallet.ledger.domain.valueobject;

import java.util.UUID;

public record EntryId(UUID value) {

    public static EntryId of(UUID uuid) {
        return new EntryId(uuid);
    }

    public static EntryId generate() {
        return new EntryId(UUID.randomUUID());
    }

    public static EntryId fromString(String s) {
        return new EntryId(UUID.fromString(s));
    }
}
