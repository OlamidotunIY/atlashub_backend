package com.atlashub.support.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidTicketStateException extends BusinessRuleException {
    public InvalidTicketStateException(String message) {
        super(message);
    }
}
