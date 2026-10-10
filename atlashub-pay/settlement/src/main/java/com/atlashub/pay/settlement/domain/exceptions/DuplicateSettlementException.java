package com.atlashub.pay.settlement.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class DuplicateSettlementException extends ConflictException {
    public DuplicateSettlementException(String providerSettlementId) {
        super("Settlement already recorded: " + providerSettlementId);
    }
}
