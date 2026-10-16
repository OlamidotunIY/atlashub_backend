package com.atlashub.hr.staff.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidEmployeeStateException extends BusinessRuleException {

    public InvalidEmployeeStateException(String message) {
        super(message);
    }
}
