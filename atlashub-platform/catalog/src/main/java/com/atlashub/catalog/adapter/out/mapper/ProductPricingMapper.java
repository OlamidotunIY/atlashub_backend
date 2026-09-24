package com.atlashub.catalog.adapter.out.mapper;

import com.atlashub.catalog.adapter.out.entity.ProductPricingJpaEntity;
import com.atlashub.catalog.domain.model.ProductPricing;
import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;

@Component
public class ProductPricingMapper {

    public ProductPricingJpaEntity toEntity(ProductPricing domain) {
        if (domain == null) {
            return null;
        }

        return new ProductPricingJpaEntity(
                domain.getId(),
                domain.getHubProductId(),
                domain.getBillingCycle().name(),
                domain.getAmount().amount(),
                domain.getAmount().currency().name(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                null // Version is managed by JPA
        );
    }

    public ProductPricing toDomain(ProductPricingJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ProductPricing(
                entity.getId(),
                entity.getHubProductId(),
                BillingCycle.valueOf(entity.getBillingCycle()),
                Money.of(entity.getAmount(), CurrencyCode.valueOf(entity.getCurrencyCode())),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
