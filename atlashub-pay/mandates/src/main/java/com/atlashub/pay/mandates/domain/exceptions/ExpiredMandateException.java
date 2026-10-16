package com.atlashub.pay.mandates.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class ExpiredMandateException extends BusinessRuleException {

    public ExpiredMandateException() {
        super("Payment mandate has expired");
    }

    public ExpiredMandateException(String message) {
        super(message);
    }
}
