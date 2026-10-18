package com.atlashub.pay.controls.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class UnsupportedControlCapabilityException extends BusinessRuleException {

    public UnsupportedControlCapabilityException() {
        super("Requested control capability is not supported by provider or rail");
    }

    public UnsupportedControlCapabilityException(String message) {
        super(message);
    }
}
