package com.atlashub.hr.payroll.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class DuplicatePayrollPeriodException extends ConflictException {

    public DuplicatePayrollPeriodException(String period) {
        super("A payroll run already exists for period: " + period);
    }
}
