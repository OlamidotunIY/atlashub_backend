package com.atlashub.billing.infrastructure.persistence.repositories;

import com.atlashub.billing.domain.valueobject.InvoiceStatus;
import com.atlashub.billing.infrastructure.persistence.entities.BillingInvoiceJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataBillingInvoiceRepository extends JpaRepository<BillingInvoiceJpa, Long> {

    Optional<BillingInvoiceJpa> findByInvoiceNumber(String invoiceNumber);

    Optional<BillingInvoiceJpa> findByPaymentReference(String paymentReference);

    List<BillingInvoiceJpa> findByOrganizationId(Long organizationId);

    List<BillingInvoiceJpa> findByOrganizationIdAndStatus(Long organizationId, InvoiceStatus status);

    List<BillingInvoiceJpa> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDate date);

    @Query("SELECT i FROM BillingInvoiceJpa i WHERE i.status = 'OVERDUE' AND i.dueDate <= :date")
    List<BillingInvoiceJpa> findOverdueInvoicesOlderThan(@Param("date") LocalDate date);
}
