package com.atlashub.billing.domain.repository;

import com.atlashub.billing.domain.model.BillingInvoice;

public interface BillingInvoiceRepository {
    Long nextIdentity();
    BillingInvoice save(BillingInvoice billingInvoice);
}
