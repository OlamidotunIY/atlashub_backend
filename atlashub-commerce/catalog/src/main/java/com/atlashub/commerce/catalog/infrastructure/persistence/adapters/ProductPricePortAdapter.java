package com.atlashub.commerce.catalog.infrastructure.persistence.adapters;

import com.atlashub.commerce.catalog.domain.valueobject.PriceLevel;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.ProductPriceJpa;
import com.atlashub.commerce.catalog.infrastructure.persistence.repositories.SpringDataProductPriceRepository;
import com.atlashub.shared.application.port.ProductPricePort;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Component
public class ProductPricePortAdapter implements ProductPricePort {

    private final SpringDataProductPriceRepository priceRepo;
    private final DomainSequenceGenerator sequenceGenerator;

    public ProductPricePortAdapter(SpringDataProductPriceRepository priceRepo,
                                  DomainSequenceGenerator sequenceGenerator) {
        this.priceRepo = Objects.requireNonNull(priceRepo, "SpringDataProductPriceRepository must not be null");
        this.sequenceGenerator = Objects.requireNonNull(sequenceGenerator, "DomainSequenceGenerator must not be null");
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("commerce_product_price_seq");
    }

    @Override
    public void savePrice(Long id, Long productId, Long variantId, String priceLevel,
                          Money costPrice, Money sellingPrice, BigDecimal markup) {
        PriceLevel level = PriceLevel.valueOf(priceLevel);
        ProductPriceJpa jpa = new ProductPriceJpa(
                id,
                productId,
                variantId,
                level,
                costPrice != null ? costPrice.amount() : null,
                sellingPrice != null ? sellingPrice.amount() : null,
                markup
        );
        priceRepo.save(jpa);
    }

    @Override
    public Optional<ProductPriceDto> findPrice(Long productId, Long variantId, String priceLevel) {
        PriceLevel level = PriceLevel.valueOf(priceLevel);
        return priceRepo.findPrice(productId, variantId, level)
                .map(this::toDto);
    }

    private ProductPriceDto toDto(ProductPriceJpa jpa) {
        return new ProductPriceDto(
                jpa.getId(),
                jpa.getProductId(),
                jpa.getVariantId(),
                jpa.getPriceLevel().name(),
                jpa.getCostPrice() != null ? Money.of(jpa.getCostPrice(), CurrencyCode.NGN) : null,
                jpa.getSellingPrice() != null ? Money.of(jpa.getSellingPrice(), CurrencyCode.NGN) : null,
                jpa.getMarkup()
        );
    }
}
