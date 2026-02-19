package com.wallet.ledger.domain.exception;

/** Thrown when a posting command is invalid (e.g. debits != credits). */
public class InvalidPostingException extends DomainException {

    public InvalidPostingException(String message) {
        super(message);
    }

    public InvalidPostingException(String message, Throwable cause) {
        super(message, cause);
    }
}
