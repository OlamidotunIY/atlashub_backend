package com.atlashub.logistics.shipping.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class RiderUnavailableException extends BusinessRuleException {

    public RiderUnavailableException() {
        super("Dispatch rider is not available for assignment");
    }

    public RiderUnavailableException(String message) {
        super(message);
    }
}
