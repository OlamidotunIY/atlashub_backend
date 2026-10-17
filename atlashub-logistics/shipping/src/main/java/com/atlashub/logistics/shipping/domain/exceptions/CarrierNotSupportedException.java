package com.atlashub.logistics.shipping.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class CarrierNotSupportedException extends BusinessRuleException {

    public CarrierNotSupportedException(String carrierCode) {
        super("Carrier not supported: " + carrierCode);
    }
}
