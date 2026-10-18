package com.atlashub.pay.transfers.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class PayoutApprovalRequiredException extends BusinessRuleException {

    public PayoutApprovalRequiredException() {
        super("This payout requires approval before execution");
    }

    public PayoutApprovalRequiredException(String message) {
        super(message);
    }
}
