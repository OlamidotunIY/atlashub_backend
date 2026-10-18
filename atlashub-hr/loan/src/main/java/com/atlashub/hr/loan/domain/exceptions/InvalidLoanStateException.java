package com.atlashub.hr.loan.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidLoanStateException extends BusinessRuleException {

    public InvalidLoanStateException(String message) {
        super(message);
    }
}
