package com.atlashub.hr.leave.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidLeaveStateException extends BusinessRuleException {

    public InvalidLeaveStateException(String message) {
        super(message);
    }
}
