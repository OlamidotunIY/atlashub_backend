package com.atlashub.catalog.domain.exception;

import com.atlashub.shared.domain.exception.ErrorCode;

public enum CatalogErrorCode implements ErrorCode {
    PRODUCT_NOT_FOUND, DUPLICATE_PRODUCT_KEY, INVALID_PRICING_MODEL, PRODUCT_NOT_ACTIVE
}
