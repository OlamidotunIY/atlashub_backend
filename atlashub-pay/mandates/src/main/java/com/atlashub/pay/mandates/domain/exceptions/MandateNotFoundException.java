package com.atlashub.pay.mandates.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class MandateNotFoundException extends NotFoundException {

    public MandateNotFoundException() {
        super("Payment mandate not found");
    }

    public MandateNotFoundException(String message) {
        super(message);
    }
}
