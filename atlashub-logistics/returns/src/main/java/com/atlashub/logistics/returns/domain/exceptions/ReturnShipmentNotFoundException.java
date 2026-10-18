package com.atlashub.logistics.returns.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class ReturnShipmentNotFoundException extends NotFoundException {

    public ReturnShipmentNotFoundException(Long id) {
        super("Return shipment not found: " + id);
    }

    public ReturnShipmentNotFoundException(String trackingNumber) {
        super("Return shipment not found for tracking number: " + trackingNumber);
    }
}
