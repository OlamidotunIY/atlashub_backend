package com.atlashub.hr.staff.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class EmployeeBankUnverifiedException extends BusinessRuleException {

    public EmployeeBankUnverifiedException() {
        super("Employee bank account has not been verified");
    }

    public EmployeeBankUnverifiedException(String message) {
        super(message);
    }
}
