package com.atlashub.pay.transfers.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class SelfApprovalNotAllowedException extends BusinessRuleException {

    public SelfApprovalNotAllowedException() {
        super("The initiator of a payout cannot approve it");
    }

    public SelfApprovalNotAllowedException(String message) {
        super(message);
    }
}
