package com.atlashub.logistics.warehousing.domain.valueobject;

import com.atlashub.shared.domain.exception.BusinessRuleException;
import java.math.BigDecimal;

public record StockTransferItem(
        Long productId,
        String productSku,
        String productName,
        int quantity,
        BigDecimal weightKg
) {
    public StockTransferItem {
        if (productId == null) {
            throw new BusinessRuleException("Product ID cannot be null");
        }
        if (quantity <= 0) {
            throw new BusinessRuleException("Quantity must be greater than zero");
        }
        if (weightKg == null || weightKg.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Weight must be non-negative");
        }
    }
}
