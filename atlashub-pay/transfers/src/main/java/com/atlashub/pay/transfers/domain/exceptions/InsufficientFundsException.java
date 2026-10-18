package com.atlashub.pay.transfers.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InsufficientFundsException extends BusinessRuleException {

    public InsufficientFundsException() {
        super("Insufficient funds for this payout");
    }

    public InsufficientFundsException(String message) {
        super(message);
    }
}
