package com.atlashub.logistics.shipping.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class VehicleNotFoundException extends NotFoundException {

    public VehicleNotFoundException(Long id) {
        super("Fleet vehicle not found: " + id);
    }
}
