package com.atlashub.logistics.shipping.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class RiderNotFoundException extends NotFoundException {

    public RiderNotFoundException(Long id) {
        super("Dispatch rider not found: " + id);
    }
}
