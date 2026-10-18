package com.atlashub.hr.loan.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class LoanAlreadyActiveException extends ConflictException {

    public LoanAlreadyActiveException() {
        super("Employee already has an active loan");
    }
}
