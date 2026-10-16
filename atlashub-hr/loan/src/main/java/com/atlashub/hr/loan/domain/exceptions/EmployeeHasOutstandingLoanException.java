package com.atlashub.hr.loan.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class EmployeeHasOutstandingLoanException extends BusinessRuleException {

    public EmployeeHasOutstandingLoanException() {
        super("Employee has an outstanding loan balance");
    }
}
