package com.atlashub.pay.settlement.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InvalidSettlementStateException extends BusinessRuleException {
    public InvalidSettlementStateException(String message) { super(message); }
}
