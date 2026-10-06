package com.atlashub.pay.splits.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class SplitRuleNotFoundException extends NotFoundException {
    public SplitRuleNotFoundException() {
        super("Split rule not found");
    }

    public SplitRuleNotFoundException(String message) {
        super(message);
    }
}
