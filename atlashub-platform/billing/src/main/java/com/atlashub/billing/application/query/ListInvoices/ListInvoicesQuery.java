package com.atlashub.billing.application.query.ListInvoices;

public record ListInvoicesQuery(
        Long organizationId,
        String status
) {
}
