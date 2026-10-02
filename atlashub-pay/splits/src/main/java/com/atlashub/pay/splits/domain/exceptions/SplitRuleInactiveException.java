package com.atlashub.pay.splits.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class SplitRuleInactiveException extends BusinessRuleException {
    public SplitRuleInactiveException() {
        super("Split rule is inactive and cannot be modified or used");
    }
}
