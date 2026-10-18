package com.atlashub.hr.leave.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InsufficientLeaveBalanceException extends BusinessRuleException {

    public InsufficientLeaveBalanceException() {
        super("Insufficient leave balance for the requested number of days");
    }

    public InsufficientLeaveBalanceException(String message) {
        super(message);
    }
}
