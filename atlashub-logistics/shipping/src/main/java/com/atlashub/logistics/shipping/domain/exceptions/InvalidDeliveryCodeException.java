package com.atlashub.logistics.shipping.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidDeliveryCodeException extends BusinessRuleException {

    public InvalidDeliveryCodeException() {
        super("Invalid delivery code provided for shipment handover");
    }

    public InvalidDeliveryCodeException(String message) {
        super(message);
    }
}
