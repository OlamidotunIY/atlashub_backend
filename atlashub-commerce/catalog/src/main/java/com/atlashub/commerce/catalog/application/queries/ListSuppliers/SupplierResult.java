package com.atlashub.commerce.catalog.application.queries.ListSuppliers;

import com.atlashub.commerce.catalog.domain.valueobject.SupplierStatus;

import java.time.ZonedDateTime;

public record SupplierResult(
        Long id,
        Long organizationId,
        String name,
        String email,
        String phone,
        String address,
        SupplierStatus status,
        ZonedDateTime createdAt
) {
}
