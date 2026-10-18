package com.atlashub.logistics.shipping.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidShipmentStatusException extends BusinessRuleException {

    public InvalidShipmentStatusException(String message) {
        super(message);
    }
}
