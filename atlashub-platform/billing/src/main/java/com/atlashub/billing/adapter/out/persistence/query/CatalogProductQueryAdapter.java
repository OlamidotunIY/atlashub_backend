package com.atlashub.billing.adapter.out.persistence.query;

import com.atlashub.billing.application.port.out.ProductPricingDto;
import com.atlashub.billing.application.port.out.ProductQueryPort;
import com.atlashub.catalog.domain.model.ProductPricing;
import com.atlashub.catalog.domain.repository.ProductPricingRepository;
import com.atlashub.shared.domain.money.CurrencyCode;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CatalogProductQueryAdapter implements ProductQueryPort {

    private final ProductPricingRepository productPricingRepository;

    public CatalogProductQueryAdapter(ProductPricingRepository productPricingRepository) {
        this.productPricingRepository = productPricingRepository;
    }

    @Override
    public Optional<ProductPricingDto> getProductPricing(Long productId, CurrencyCode currency) {
        if (productId == null) return Optional.empty();
        
        Optional<ProductPricing> productPricing = productPricingRepository.findByProductIdAndCurrency(productId, currency);
        if (productPricing.isEmpty()) {
            return Optional.empty();
        }
        
        // For simplicity, we just take the first pricing available
        ProductPricing pricing = productPricing.get();
        
        return Optional.of(new ProductPricingDto(
                productId,
                pricing.getAmount().amount(),
                pricing.getAmount().currency().name(),
                pricing.getBillingCycle().name()
        ));
    }
}
