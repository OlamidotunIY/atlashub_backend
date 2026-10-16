package com.atlashub.pay.transfers.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidPayoutStateException extends BusinessRuleException {

    public InvalidPayoutStateException() {
        super("Invalid payout state");
    }

    public InvalidPayoutStateException(String message) {
        super(message);
    }
}
