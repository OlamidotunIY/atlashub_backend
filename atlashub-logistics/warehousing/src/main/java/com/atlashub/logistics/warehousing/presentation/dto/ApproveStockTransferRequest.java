package com.atlashub.logistics.warehousing.presentation.dto;

import com.atlashub.shared.domain.valueobject.Money;
import jakarta.validation.constraints.NotNull;

public record ApproveStockTransferRequest(
        @NotNull(message = "Declared value is required")
        Money declaredValue
) {}
