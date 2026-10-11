package com.atlashub.accounting.cash.domain.entities;

import com.atlashub.accounting.cash.domain.exceptions.InvalidReconciliationStateException;
import lombok.Getter;

@Getter
public class ReconciliationItem {

    private final Long id;
    private final Long reconciliationId;
    private final Long bankStatementLineId;
    private final Long journalLineId;

    public ReconciliationItem(Long id,
                              Long reconciliationId,
                              Long bankStatementLineId,
                              Long journalLineId) {
        this.id = id;
        this.reconciliationId = reconciliationId;
        this.bankStatementLineId = bankStatementLineId;
        this.journalLineId = journalLineId;
    }

    public static ReconciliationItem create(Long id,
                                            Long reconciliationId,
                                            Long bankStatementLineId,
                                            Long journalLineId) {
        if (id == null) {
            throw new InvalidReconciliationStateException("Reconciliation item ID must not be null");
        }
        if (reconciliationId == null) {
            throw new InvalidReconciliationStateException("Reconciliation ID must not be null");
        }
        if (bankStatementLineId == null) {
            throw new InvalidReconciliationStateException("Bank statement line ID must not be null");
        }
        if (journalLineId == null) {
            throw new InvalidReconciliationStateException("Journal line ID must not be null");
        }
        return new ReconciliationItem(id, reconciliationId, bankStatementLineId, journalLineId);
    }
}
