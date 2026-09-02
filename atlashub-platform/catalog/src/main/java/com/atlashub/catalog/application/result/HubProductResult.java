package com.atlashub.catalog.application.result;

import com.atlashub.catalog.domain.model.HubProduct;
import com.atlashub.catalog.domain.valueobject.ProductKey;
import com.atlashub.catalog.domain.valueobject.ProductStatus;

import java.time.ZonedDateTime;

public record HubProductResult(
        Long id,
        ProductKey key,
        String name,
        String description,
        ProductStatus status,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static HubProductResult from(HubProduct product) {
        return new HubProductResult(
                product.getId(),
                product.getKey(),
                product.getName(),
                product.getDescription(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
