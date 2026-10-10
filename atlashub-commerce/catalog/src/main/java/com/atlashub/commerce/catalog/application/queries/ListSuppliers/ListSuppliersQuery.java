package com.atlashub.commerce.catalog.application.queries.ListSuppliers;

import com.atlashub.commerce.catalog.domain.valueobject.SupplierStatus;

public record ListSuppliersQuery(Long organizationId, SupplierStatus status) {
}
