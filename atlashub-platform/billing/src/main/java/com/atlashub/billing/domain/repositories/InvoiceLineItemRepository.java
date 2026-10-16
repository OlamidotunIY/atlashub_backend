package com.atlashub.billing.domain.repositories;

import com.atlashub.billing.domain.entities.InvoiceLineItem;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;

public interface InvoiceLineItemRepository extends Repository<InvoiceLineItem> {

    List<InvoiceLineItem> findByInvoiceId(Long invoiceId);
}
