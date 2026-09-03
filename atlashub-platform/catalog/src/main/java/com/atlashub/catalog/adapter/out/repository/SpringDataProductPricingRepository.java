package com.atlashub.catalog.adapter.out.repository;

import com.atlashub.catalog.adapter.out.entity.ProductPricingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataProductPricingRepository extends JpaRepository<ProductPricingJpaEntity, Long> {
    Optional<ProductPricingJpaEntity> findByHubProductIdAndBillingCycleAndCurrencyCode(Long hubProductId, String billingCycle, String currencyCode);
    List<ProductPricingJpaEntity> findAllByHubProductId(Long hubProductId);
    Optional<ProductPricingJpaEntity> findByProductIdAndCurrency(Long productId, String currency);
}
