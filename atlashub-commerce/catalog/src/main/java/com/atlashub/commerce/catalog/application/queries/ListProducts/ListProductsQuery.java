package com.atlashub.commerce.catalog.application.queries.ListProducts;

import com.atlashub.commerce.catalog.domain.valueobject.ProductStatus;

public record ListProductsQuery(
        Long organizationId,
        Long vendorId,
        Long categoryId,
        ProductStatus status,
        String search,
        int page,
        int size
) {
}
