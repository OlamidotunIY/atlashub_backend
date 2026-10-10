package com.atlashub.pay.settlement.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class SettlementNotFoundException extends NotFoundException {
    public SettlementNotFoundException(Long id) { super("Settlement not found: " + id); }
    public SettlementNotFoundException(String providerSettlementId) {
        super("Settlement not found: " + providerSettlementId);
    }
}
