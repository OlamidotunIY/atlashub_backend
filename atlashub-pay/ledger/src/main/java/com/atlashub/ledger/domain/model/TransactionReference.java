package com.atlashub.ledger.domain.model;

import com.atlashub.ledger.domain.valueobject.SourceSystem;

public record TransactionReference(
        String transactionId,
        SourceSystem sourceSystem
) {
    public TransactionReference {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("transactionId must not be empty");
        }
        if (sourceSystem == null) {
            throw new IllegalArgumentException("sourceSystem must not be null");
        }
    }
}
