package com.atlashub.logistics.warehousing.application.command.RequestStockTransfer;

import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;

public record RequestStockTransferResult(
        Long transferId,
        String status,
        BigDecimal totalWeightKg,
        Money transferShippingFee
) {}
