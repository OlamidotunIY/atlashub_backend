package com.atlashub.logistics.shipping.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class VehicleUnavailableException extends BusinessRuleException {

    public VehicleUnavailableException() {
        super("Fleet vehicle is not available for assignment");
    }

    public VehicleUnavailableException(String message) {
        super(message);
    }
}
