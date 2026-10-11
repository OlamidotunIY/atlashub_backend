package com.atlashub.accounting.cash.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class CashEvacuationNotFoundException extends NotFoundException {

    public CashEvacuationNotFoundException(Long id) {
        super("Cash evacuation not found: " + id);
    }
}
