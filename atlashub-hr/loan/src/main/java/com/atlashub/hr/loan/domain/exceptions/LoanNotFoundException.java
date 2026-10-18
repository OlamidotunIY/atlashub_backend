package com.atlashub.hr.loan.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class LoanNotFoundException extends NotFoundException {

    public LoanNotFoundException(Long id) {
        super("Loan not found: " + id);
    }
}
