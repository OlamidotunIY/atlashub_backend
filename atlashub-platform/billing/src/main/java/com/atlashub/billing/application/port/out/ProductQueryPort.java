package com.atlashub.billing.application.port.out;

import com.atlashub.shared.domain.money.CurrencyCode;

import java.util.Optional;

public interface ProductQueryPort {
    Optional<ProductPricingDto> getProductPricing(Long productId, CurrencyCode currency);
}