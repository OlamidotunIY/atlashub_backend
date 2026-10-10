package com.atlashub.commerce.catalog.application.commands.CreateProduct;

import com.atlashub.commerce.catalog.domain.valueobject.ProductType;

public record CreateProductCommand(
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
        ProductType type
) {
}
