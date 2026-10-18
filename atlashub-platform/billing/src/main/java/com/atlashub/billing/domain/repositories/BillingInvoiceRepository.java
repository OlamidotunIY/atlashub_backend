package com.atlashub.billing.domain.repositories;

import com.atlashub.billing.domain.entities.BillingInvoice;
import com.atlashub.billing.domain.valueobject.InvoiceStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillingInvoiceRepository extends Repository<BillingInvoice> {

    Optional<BillingInvoice> findByInvoiceNumber(String invoiceNumber);

    Optional<BillingInvoice> findByPaymentReference(String paymentReference);

    List<BillingInvoice> findByOrganizationId(Long organizationId);

    List<BillingInvoice> findByOrganizationIdAndStatus(Long organizationId, InvoiceStatus status);

    List<BillingInvoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDate date);

    List<BillingInvoice> findOverdueInvoicesOlderThan(LocalDate date);
}
