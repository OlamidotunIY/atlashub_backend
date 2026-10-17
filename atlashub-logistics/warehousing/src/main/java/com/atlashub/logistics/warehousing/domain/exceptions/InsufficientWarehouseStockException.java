package com.atlashub.logistics.warehousing.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class InsufficientWarehouseStockException extends BusinessRuleException {

    public InsufficientWarehouseStockException(Long warehouseId, Long productId, int requested, int available) {
        super(String.format("Insufficient stock in warehouse %d for product %d: requested %d, available %d",
                warehouseId, productId, requested, available));
    }
}
