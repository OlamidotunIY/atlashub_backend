package com.atlashub.catalog.application.usecase;

import com.atlashub.catalog.application.command.SetProductPricingCommand;
import com.atlashub.catalog.application.result.SetProductPricingResult;
import com.atlashub.catalog.domain.exception.CatalogErrorCode;
import com.atlashub.catalog.domain.model.HubProduct;
import com.atlashub.catalog.domain.model.ProductPricing;
import com.atlashub.catalog.domain.repository.HubProductRepository;
import com.atlashub.catalog.domain.repository.ProductPricingRepository;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public class SetProductPricingUseCase extends BaseUseCase<SetProductPricingCommand, SetProductPricingResult> {

    private static final Logger log = LoggerFactory.getLogger(SetProductPricingUseCase.class);

    private final HubProductRepository hubProductRepository;
    private final ProductPricingRepository productPricingRepository;

    public SetProductPricingUseCase(
            HubProductRepository hubProductRepository,
            ProductPricingRepository productPricingRepository) {
        this.hubProductRepository = hubProductRepository;
        this.productPricingRepository = productPricingRepository;
    }

    @Override
    @Transactional
    public SetProductPricingResult execute(SetProductPricingCommand input) {
        log.info("Setting pricing for product {} — cycle: {}, currency: {}",
                input.productId(), input.cycle(), input.amount().currency());

        // 1. Guard: the target HubProduct must exist and be ACTIVE
        HubProduct product = hubProductRepository.findById(input.productId())
                .orElseThrow(() -> new NotFoundException(
                        CatalogErrorCode.PRODUCT_NOT_FOUND,
                        "HubProduct not found with id: " + input.productId()));

        if (!product.getStatus().name().equals("ACTIVE")) {
            throw new com.atlashub.shared.domain.exception.BusinessRuleException(
                    CatalogErrorCode.PRODUCT_NOT_ACTIVE,
                    "Pricing can only be set on an ACTIVE product");
        }

        // 2. Upsert: find existing pricing row keyed by (productId, billingCycle, currency)
        Optional<ProductPricing> existing = productPricingRepository
                .findByProductIdAndCycleAndCurrency(
                        input.productId(),
                        input.cycle(),
                        input.amount().currency());

        if (existing.isPresent()) {
            // --- UPDATE path ---
            ProductPricing pricing = existing.get();
            log.info("Existing pricing row found (id: {}), updating price from {} to {}",
                    pricing.getId(), pricing.getAmount(), input.amount());

            pricing.updatePrice(input.amount());
            productPricingRepository.save(pricing);

            log.info("ProductPricing updated successfully");
            return SetProductPricingResult.updated(pricing);

        } else {
            // --- CREATE path ---
            log.info("No existing pricing row found — creating new ProductPricing");

            ProductPricing pricing = ProductPricing.create(
                    productPricingRepository.nextIdentity(),
                    input.productId(),
                    input.cycle(),
                    input.amount()
            );

            productPricingRepository.save(pricing);

            log.info("ProductPricing created successfully with id: {}", pricing.getId());
            return SetProductPricingResult.created(pricing);
        }
    }
}
