package com.atlashub.ledger.domain.repository;

import com.atlashub.ledger.domain.model.LedgerTransaction;
import com.atlashub.ledger.domain.model.SourceSystem;

public interface LedgerTransactionRepository {
    Long nextIdentity();
    LedgerTransaction save(LedgerTransaction transaction);
    boolean existsByReference(String transactionId, SourceSystem sourceSystem);
}
