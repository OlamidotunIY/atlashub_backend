package com.atlashub.logistics.shipping.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class UrgentShipmentBatchingForbiddenException extends BusinessRuleException {

    public UrgentShipmentBatchingForbiddenException() {
        super("Urgent shipments cannot be assigned to a consolidation batch and must be dispatched immediately");
    }

    public UrgentShipmentBatchingForbiddenException(String message) {
        super(message);
    }
}
