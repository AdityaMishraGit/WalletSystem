package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.entity.Transaction;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PostingResult {

    Transaction transaction;

    public static PostingResult of(Transaction transaction) {
        return PostingResult.builder().transaction(transaction).build();
    }
}
