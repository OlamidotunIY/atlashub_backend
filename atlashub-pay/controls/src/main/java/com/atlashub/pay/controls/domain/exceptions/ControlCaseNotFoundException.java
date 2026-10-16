package com.atlashub.pay.controls.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class ControlCaseNotFoundException extends NotFoundException {

    public ControlCaseNotFoundException() {
        super("Control case not found");
    }

    public ControlCaseNotFoundException(String message) {
        super(message);
    }
}
