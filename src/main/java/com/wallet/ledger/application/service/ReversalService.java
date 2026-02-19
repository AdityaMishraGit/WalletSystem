package com.wallet.ledger.application.service;

import com.wallet.ledger.application.port.*;
import com.wallet.ledger.domain.entity.LedgerEntry;
import com.wallet.ledger.domain.valueobject.EntryDirection;
import com.wallet.ledger.domain.valueobject.TransactionId;
import com.wallet.ledger.domain.valueobject.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReversalService {

    private final FindTransactionByReferencePort findTransactionByReferencePort;
    private final FindLedgerEntriesByTransactionIdPort findLedgerEntriesByTransactionIdPort;
    private final LedgerPostingEngine ledgerPostingEngine;

    public PostingResult reverse(String originalReferenceId, String reversalReferenceId) {
        log.debug("Reversal originalRef={} reversalRef={}", originalReferenceId, reversalReferenceId);
        var originalTxn = findTransactionByReferencePort.findByReferenceId(originalReferenceId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + originalReferenceId));
        List<LedgerEntry> entries = findLedgerEntriesByTransactionIdPort.findByTransactionId(originalTxn.getTransactionId());
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("No ledger entries for transaction: " + originalReferenceId);
        }
        List<PostingLeg> reverseLegs = entries.stream()
                .map(e -> PostingLeg.builder()
                        .accountId(e.getAccountId())
                        .direction(e.getDirection() == EntryDirection.DEBIT ? EntryDirection.CREDIT : EntryDirection.DEBIT)
                        .amount(e.getAmount())
                        .build())
                .collect(Collectors.toList());
        PostingCommand cmd = PostingCommand.builder()
                .transactionId(TransactionId.generate())
                .transactionType(TransactionType.REVERSAL)
                .referenceId(reversalReferenceId)
                .legs(reverseLegs)
                .build();
        PostingResult result = ledgerPostingEngine.post(cmd);
        log.info("Reversal completed originalRef={} reversalTxnId={}", originalReferenceId, result.getTransaction().getTransactionId().value());
        return result;
    }
}
