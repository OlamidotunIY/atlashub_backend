package com.atlashub.commerce.catalog.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class VendorSuspendedException extends BusinessRuleException {
    public VendorSuspendedException() {
        super("Vendor is suspended and cannot list products");
    }

    public VendorSuspendedException(String message) {
        super(message);
    }
}
