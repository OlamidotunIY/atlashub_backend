package com.atlashub.accounting.gl.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.domain.valueobject.Money;

public class EntryAmountExceedsApprovalThresholdException extends BusinessRuleException {

    public EntryAmountExceedsApprovalThresholdException(Money threshold) {
        super("Manual entry amount exceeds approval threshold of " + threshold + " - must be submitted for approval");
    }
}
