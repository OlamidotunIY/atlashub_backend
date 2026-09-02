package com.atlashub.catalog.domain.repository;

import com.atlashub.catalog.domain.model.ProductPricing;

import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.shared.money.CurrencyCode;

import java.util.List;
import java.util.Optional;

public interface ProductPricingRepository {
    Long nextIdentity();
    ProductPricing save(ProductPricing productPricing);
    Optional<ProductPricing> findById(Long id);
    Optional<ProductPricing> findByProductIdAndCycleAndCurrency(Long productId, BillingCycle cycle, CurrencyCode currency);
    List<ProductPricing> findAllByProductId(Long productId);
}
