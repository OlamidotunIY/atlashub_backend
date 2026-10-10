package com.atlashub.commerce.catalog.application.commands.SetProductPrice;

import com.atlashub.commerce.catalog.domain.valueobject.PriceLevel;
import com.atlashub.shared.domain.valueobject.Money;

public record SetProductPriceCommand(
        Long productId,
        Long variantId,
        PriceLevel priceLevel,
        Money costPrice,
        Money sellingPrice
) {
}
