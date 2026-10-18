package com.atlashub.logistics.shipping.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidMilestoneTransitionException extends BusinessRuleException {

    public InvalidMilestoneTransitionException(String message) {
        super(message);
    }
}
