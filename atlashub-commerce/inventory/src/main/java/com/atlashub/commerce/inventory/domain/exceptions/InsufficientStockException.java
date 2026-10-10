package com.atlashub.commerce.inventory.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InsufficientStockException extends BusinessRuleException {

    public InsufficientStockException(Long productId, int requested, int available) {
        super("Insufficient stock for product " + productId + ": requested " + requested + ", available " + available);
    }
}
