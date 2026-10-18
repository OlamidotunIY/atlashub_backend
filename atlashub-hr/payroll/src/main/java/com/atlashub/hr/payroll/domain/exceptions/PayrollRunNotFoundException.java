package com.atlashub.hr.payroll.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class PayrollRunNotFoundException extends NotFoundException {

    public PayrollRunNotFoundException(Long id) {
        super("Payroll run not found: " + id);
    }
}
