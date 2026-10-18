package com.atlashub.pay.controls.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class PolicyNotFoundException extends NotFoundException {

    public PolicyNotFoundException() {
        super("Controlled funds policy not found");
    }

    public PolicyNotFoundException(String message) {
        super(message);
    }
}
