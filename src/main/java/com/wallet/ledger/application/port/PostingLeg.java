package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.valueobject.AccountId;
import com.wallet.ledger.domain.valueobject.EntryDirection;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/** Single leg of a double-entry posting (one DEBIT or one CREDIT). */
@Value
@Builder
public class PostingLeg {

    AccountId accountId;
    EntryDirection direction;
    BigDecimal amount;

    public AccountId getAccountId() { return accountId; }
    public EntryDirection getDirection() { return direction; }
    public BigDecimal getAmount() { return amount; }
}
