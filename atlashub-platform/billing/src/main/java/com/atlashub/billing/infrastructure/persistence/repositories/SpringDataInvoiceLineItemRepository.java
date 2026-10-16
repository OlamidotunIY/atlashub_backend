package com.atlashub.billing.infrastructure.persistence.repositories;

import com.atlashub.billing.infrastructure.persistence.entities.InvoiceLineItemJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataInvoiceLineItemRepository extends JpaRepository<InvoiceLineItemJpa, Long> {

    List<InvoiceLineItemJpa> findByInvoiceId(Long invoiceId);
}
