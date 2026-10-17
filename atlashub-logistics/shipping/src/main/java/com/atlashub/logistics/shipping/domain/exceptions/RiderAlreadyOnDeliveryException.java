package com.atlashub.logistics.shipping.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class RiderAlreadyOnDeliveryException extends ConflictException {

    public RiderAlreadyOnDeliveryException() {
        super("Dispatch rider already has an active delivery");
    }

    public RiderAlreadyOnDeliveryException(String message) {
        super(message);
    }
}
