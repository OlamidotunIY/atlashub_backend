package com.atlashub.accounting.cash.domain.entities;

import com.atlashub.accounting.cash.domain.exceptions.InvalidReconciliationStateException;
import com.atlashub.accounting.cash.domain.valueobject.StatementEntryType;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class BankStatementLine {

    private final Long id;
    private final Long reconciliationId;
    private final LocalDate transactionDate;
    private final String description;
    private final Money amount;
    private final StatementEntryType type;

    public BankStatementLine(Long id,
                             Long reconciliationId,
                             LocalDate transactionDate,
                             String description,
                             Money amount,
                             StatementEntryType type) {
        this.id = id;
        this.reconciliationId = reconciliationId;
        this.transactionDate = transactionDate;
        this.description = description;
        this.amount = amount;
        this.type = type;
    }

    public static BankStatementLine create(Long id,
                                           Long reconciliationId,
                                           LocalDate transactionDate,
                                           String description,
                                           Money amount,
                                           StatementEntryType type) {
        if (id == null) {
            throw new InvalidReconciliationStateException("Statement line ID must not be null");
        }
        if (reconciliationId == null) {
            throw new InvalidReconciliationStateException("Reconciliation ID must not be null");
        }
        if (transactionDate == null) {
            throw new InvalidReconciliationStateException("Transaction date must not be null");
        }
        if (amount == null || !amount.isPositive()) {
            throw new InvalidReconciliationStateException("Statement line amount must be positive");
        }
        if (type == null) {
            throw new InvalidReconciliationStateException("Statement entry type must not be null");
        }
        return new BankStatementLine(id, reconciliationId, transactionDate, description, amount, type);
    }
}
