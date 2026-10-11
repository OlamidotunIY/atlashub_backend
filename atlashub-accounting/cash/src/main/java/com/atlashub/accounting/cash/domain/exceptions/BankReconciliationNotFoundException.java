package com.atlashub.accounting.cash.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class BankReconciliationNotFoundException extends NotFoundException {

    public BankReconciliationNotFoundException(Long id) {
        super("Bank reconciliation not found: " + id);
    }
}
