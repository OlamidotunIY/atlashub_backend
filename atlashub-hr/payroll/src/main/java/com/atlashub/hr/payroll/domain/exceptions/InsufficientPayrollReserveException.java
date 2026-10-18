package com.atlashub.hr.payroll.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InsufficientPayrollReserveException extends BusinessRuleException {

    public InsufficientPayrollReserveException() {
        super("Payroll reserve account has insufficient funds to cover the payroll run");
    }
}
