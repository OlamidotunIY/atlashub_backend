package com.atlashub.commerce.catalog.application.queries.ListVendors;

import com.atlashub.commerce.catalog.domain.valueobject.VendorStatus;

public record ListVendorsQuery(Long organizationId, VendorStatus status) {
}
