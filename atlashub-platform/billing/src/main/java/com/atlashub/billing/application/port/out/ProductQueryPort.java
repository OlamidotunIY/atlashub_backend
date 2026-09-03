package com.atlashub.billing.application.port.out;

import java.util.Optional;

public interface ProductQueryPort {
    Optional<ProductPricingDto> getProductPricing(Long productId);
}