package com.atlashub.shared.application.port;

import com.atlashub.shared.domain.valueobject.Money;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProductPricePort {

    Long nextIdentity();

    void savePrice(Long id, Long productId, Long variantId, String priceLevel,
                   Money costPrice, Money sellingPrice, BigDecimal markup);

    Optional<ProductPriceDto> findPrice(Long productId, Long variantId, String priceLevel);

    record ProductPriceDto(
            Long id,
            Long productId,
            Long variantId,
            String priceLevel,
            Money costPrice,
            Money sellingPrice,
            BigDecimal markup
    ) {
    }
}
