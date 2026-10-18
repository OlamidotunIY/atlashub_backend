package com.atlashub.pay.controls.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class DecisionNotFoundException extends NotFoundException {

    public DecisionNotFoundException() {
        super("Payment control decision not found");
    }

    public DecisionNotFoundException(String message) {
        super(message);
    }
}
