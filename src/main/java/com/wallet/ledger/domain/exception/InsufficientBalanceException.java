package com.wallet.ledger.domain.exception;

public class InsufficientBalanceException extends DomainException {

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
