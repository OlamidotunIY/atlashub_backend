package com.atlashub.catalog.domain.repository;

import com.atlashub.catalog.domain.model.ProductPricing;

import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.shared.domain.money.CurrencyCode;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface ProductPricingRepository extends Repository<ProductPricing> {
    Long nextIdentity();
    Optional<ProductPricing> findByProductIdAndCycleAndCurrency(Long productId, BillingCycle cycle, CurrencyCode currency);
    Optional<ProductPricing> findByProductIdAndCurrency(Long productId, CurrencyCode currency);
    List<ProductPricing> findAllByProductId(Long productId);
}
