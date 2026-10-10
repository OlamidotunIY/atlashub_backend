package com.atlashub.accounting.gl.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class LedgerMappingNotFoundException extends NotFoundException {

    public LedgerMappingNotFoundException(String sourceSystem) {
        super("No ledger account mapping found for source system: " + sourceSystem);
    }
}
