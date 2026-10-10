package com.atlashub.commerce.catalog.application.queries.ListProducts;

import com.atlashub.commerce.catalog.domain.valueobject.ProductStatus;
import com.atlashub.commerce.catalog.domain.valueobject.ProductType;

import java.time.ZonedDateTime;

public record ProductResult(
        Long id,
        Long organizationId,
        Long vendorId,
        String code,
        String name,
        String description,
        Long categoryId,
        Long departmentId,
        Long manufacturerId,
        boolean taxable,
        boolean service,
        boolean hasVariants,
        ProductStatus status,
        ProductType type,
        ZonedDateTime createdAt
) {
}
