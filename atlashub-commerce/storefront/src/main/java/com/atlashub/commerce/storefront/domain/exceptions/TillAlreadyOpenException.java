package com.atlashub.commerce.storefront.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class TillAlreadyOpenException extends ConflictException {

    public TillAlreadyOpenException() {
        super("A till is already open for this outlet");
    }

    public TillAlreadyOpenException(Long outletId) {
        super("A till is already open for outlet: " + outletId);
    }
}
