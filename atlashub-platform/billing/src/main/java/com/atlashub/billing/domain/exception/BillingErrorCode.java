package com.atlashub.billing.domain.exception;

import com.atlashub.shared.exception.ErrorCode;

public enum BillingErrorCode implements ErrorCode {
    SUBSCRIPTION_ALREADY_EXISTS,
    SUBSCRIPTION_NOT_FOUND,
    INVALID_SUBSCRIPTION_STATE,
    INVOICE_NOT_FOUND,
    INVOICE_ALREADY_PAID;
}
