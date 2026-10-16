package com.atlashub.hr.staff.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class EmployeeHasOutstandingLoanException extends BusinessRuleException {

    public EmployeeHasOutstandingLoanException() {
        super("Employee has an outstanding loan balance and cannot be terminated");
    }
}
