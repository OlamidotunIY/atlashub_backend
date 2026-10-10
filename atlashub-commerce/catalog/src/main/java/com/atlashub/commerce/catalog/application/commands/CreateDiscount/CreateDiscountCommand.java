package com.atlashub.commerce.catalog.application.commands.CreateDiscount;

import com.atlashub.commerce.catalog.domain.valueobject.DiscountScope;
import com.atlashub.commerce.catalog.domain.valueobject.DiscountType;
import com.atlashub.shared.domain.valueobject.Money;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDiscountCommand(
        Long organizationId,
        String name,
        DiscountType type,
        BigDecimal value,
        DiscountScope scope,
        Money minOrderAmount,
        Integer maxUses,
        LocalDate validFrom,
        LocalDate validTo
) {
}
