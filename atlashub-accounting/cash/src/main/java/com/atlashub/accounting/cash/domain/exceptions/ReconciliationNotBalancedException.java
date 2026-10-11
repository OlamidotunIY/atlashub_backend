package com.atlashub.accounting.cash.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.domain.valueobject.Money;

public class ReconciliationNotBalancedException extends BusinessRuleException {

    public ReconciliationNotBalancedException(Money difference) {
        super("Cannot finalize reconciliation: unresolved difference of " + difference);
    }
}
