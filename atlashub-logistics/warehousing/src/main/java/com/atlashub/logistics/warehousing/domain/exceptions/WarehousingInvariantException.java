package com.atlashub.logistics.warehousing.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class WarehousingInvariantException extends BusinessRuleException {

    public WarehousingInvariantException(String message) {
        super(message);
    }
}
