package com.wallet.ledger.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount) {

    private static final int SCALE = 2;

    public Money {
        Objects.requireNonNull(amount, "amount");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money amount cannot be negative: " + amount);
        }
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount.setScale(SCALE, RoundingMode.HALF_UP));
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount).setScale(SCALE, RoundingMode.HALF_UP));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public BigDecimal toBigDecimal() {
        return amount;
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }
}
