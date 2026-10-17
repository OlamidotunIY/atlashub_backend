package com.atlashub.logistics.shipping.presentation.dto;

import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.shared.domain.valueobject.Money;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ShippingQuotesRequest(
        @NotNull Address originAddress,
        @NotNull Address destinationAddress,
        BigDecimal weightKg,
        Money declaredValue
) {}
