package com.atlashub.billing.domain.repository;

import com.atlashub.billing.domain.model.BillingInvoice;
import com.atlashub.shared.domain.repository.Repository;

public interface BillingInvoiceRepository extends Repository<BillingInvoice> {
    Long nextIdentity();
}