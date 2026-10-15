package com.atlashub.accounting.ap.application.query.ListSupplierInvoices;

import com.atlashub.accounting.ap.domain.valueobject.ApStatus;

public record ListSupplierInvoicesQuery(
        Long organizationId,
        Long supplierId,
        ApStatus status
) {}
