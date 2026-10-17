package com.atlashub.logistics.shipping.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class ProofOfDeliveryRequiredException extends BusinessRuleException {

    public ProofOfDeliveryRequiredException() {
        super("Proof of delivery URL is required to complete delivery");
    }

    public ProofOfDeliveryRequiredException(String message) {
        super(message);
    }
}
