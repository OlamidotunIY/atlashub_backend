package com.atlashub.logistics.shipping.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class ShipmentNotFoundException extends NotFoundException {

    public ShipmentNotFoundException(Long id) {
        super("Shipment not found: " + id);
    }

    public ShipmentNotFoundException(String trackingNumber) {
        super("Shipment not found for tracking number: " + trackingNumber);
    }
}
