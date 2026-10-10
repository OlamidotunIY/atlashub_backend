package com.atlashub.commerce.catalog.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class ProductPendingApprovalException extends BusinessRuleException {
    public ProductPendingApprovalException() {
        super("Product is pending approval and cannot be sold");
    }

    public ProductPendingApprovalException(String message) {
        super(message);
    }
}
