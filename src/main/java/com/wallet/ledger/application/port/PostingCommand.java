package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.valueobject.TransactionId;
import com.wallet.ledger.domain.valueobject.TransactionType;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/** Command for the ledger posting engine. Double-entry: sum of debits must equal sum of credits. */
@Value
@Builder
public class PostingCommand {

    TransactionId transactionId;
    TransactionType transactionType;
    String referenceId;
    List<PostingLeg> legs;
}
