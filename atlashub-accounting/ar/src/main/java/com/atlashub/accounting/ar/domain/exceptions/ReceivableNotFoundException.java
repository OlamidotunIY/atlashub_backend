package com.atlashub.accounting.ar.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class ReceivableNotFoundException extends NotFoundException {

    public ReceivableNotFoundException(Long id) {
        super("Customer receivable not found: " + id);
    }
}
