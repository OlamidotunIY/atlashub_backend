package com.atlashub.accounting.gl.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.domain.valueobject.Money;

public class JournalUnbalancedException extends BusinessRuleException {

    public JournalUnbalancedException(Money totalDebits, Money totalCredits) {
        super("Journal entry is unbalanced: debits=" + totalDebits + " credits=" + totalCredits);
    }
}
