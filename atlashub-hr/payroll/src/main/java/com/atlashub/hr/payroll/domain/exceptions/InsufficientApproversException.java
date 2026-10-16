package com.atlashub.hr.payroll.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InsufficientApproversException extends BusinessRuleException {

    public InsufficientApproversException() {
        super("At least 2 members with hr:payroll:approve permission are required before initiating payroll");
    }
}
