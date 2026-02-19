package com.wallet.ledger.domain.valueobject;

import java.util.UUID;

public record WalletId(UUID value) {

    public static WalletId of(UUID uuid) {
        return new WalletId(uuid);
    }

    public static WalletId generate() {
        return new WalletId(UUID.randomUUID());
    }

    public static WalletId fromString(String s) {
        return new WalletId(UUID.fromString(s));
    }
}
