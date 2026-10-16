package com.atlashub.hr.payroll.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidPayrollStateException extends BusinessRuleException {

    public InvalidPayrollStateException(String message) {
        super(message);
    }
}
