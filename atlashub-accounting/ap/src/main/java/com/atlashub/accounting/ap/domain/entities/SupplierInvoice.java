package com.atlashub.accounting.ap.domain.entities;

import com.atlashub.accounting.ap.domain.events.SupplierInvoiceCreatedEvent;
import com.atlashub.accounting.ap.domain.events.SupplierInvoicePaidEvent;
import com.atlashub.accounting.ap.domain.exceptions.InvalidInvoiceOperationException;
import com.atlashub.accounting.ap.domain.exceptions.InvoiceAlreadyPaidException;
import com.atlashub.accounting.ap.domain.exceptions.InvoiceDisputedException;
import com.atlashub.accounting.ap.domain.valueobject.ApStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
public class SupplierInvoice extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long supplierId;
    private final Long purchaseOrderId;
    private final String invoiceNumber;
    private final Money amount;
    private final LocalDate dueDate;
    private ApStatus status;
    private ZonedDateTime paidAt;
    private final ZonedDateTime createdAt;

    public SupplierInvoice(Long id,
                           Long organizationId,
                           Long supplierId,
                           Long purchaseOrderId,
                           String invoiceNumber,
                           Money amount,
                           LocalDate dueDate,
                           ApStatus status,
                           ZonedDateTime paidAt,
                           ZonedDateTime createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.supplierId = supplierId;
        this.purchaseOrderId = purchaseOrderId;
        this.invoiceNumber = invoiceNumber;
        this.amount = amount;
        this.dueDate = dueDate;
        this.status = status;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
    }

    public static SupplierInvoice create(Long id,
                                         Long organizationId,
                                         Long supplierId,
                                         Long purchaseOrderId,
                                         String invoiceNumber,
                                         Money amount,
                                         LocalDate dueDate) {
        if (id == null) {
            throw new InvalidInvoiceOperationException("Supplier invoice ID must not be null");
        }
        if (organizationId == null) {
            throw new InvalidInvoiceOperationException("Organization ID must not be null");
        }
        if (supplierId == null) {
            throw new InvalidInvoiceOperationException("Supplier ID must not be null");
        }
        if (purchaseOrderId == null) {
            throw new InvalidInvoiceOperationException("Purchase order ID must not be null");
        }
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            throw new InvalidInvoiceOperationException("Invoice number must not be blank");
        }
        if (amount == null || !amount.isPositive()) {
            throw new InvalidInvoiceOperationException("Invoice amount must be positive");
        }
        if (dueDate == null) {
            throw new InvalidInvoiceOperationException("Due date must not be null");
        }

        ZonedDateTime now = ZonedDateTime.now();
        SupplierInvoice invoice = new SupplierInvoice(
                id,
                organizationId,
                supplierId,
                purchaseOrderId,
                invoiceNumber.trim(),
                amount,
                dueDate,
                ApStatus.UNPAID,
                null,
                now
        );

        invoice.registerEvent(SupplierInvoiceCreatedEvent.of(
                id,
                organizationId,
                supplierId,
                purchaseOrderId,
                invoiceNumber.trim(),
                amount.amount(),
                amount.currency().name(),
                dueDate
        ));

        return invoice;
    }

    public void markPaid(String payoutReference, ZonedDateTime paidAtTimestamp) {
        if (this.status == ApStatus.PAID) {
            throw new InvoiceAlreadyPaidException();
        }
        if (this.status == ApStatus.DISPUTED) {
            throw new InvoiceDisputedException();
        }

        this.status = ApStatus.PAID;
        this.paidAt = paidAtTimestamp != null ? paidAtTimestamp : ZonedDateTime.now();

        registerEvent(SupplierInvoicePaidEvent.of(
                this.id,
                this.organizationId,
                this.supplierId,
                payoutReference,
                this.paidAt
        ));
    }

    public void dispute(String reason) {
        if (this.status == ApStatus.PAID) {
            throw new InvalidInvoiceOperationException("Cannot dispute a paid invoice");
        }
        if (this.status == ApStatus.DISPUTED) {
            throw new InvoiceDisputedException("Invoice is already under dispute");
        }

        this.status = ApStatus.DISPUTED;
    }

    public void resolveDispute() {
        if (this.status != ApStatus.DISPUTED) {
            throw new InvalidInvoiceOperationException("Invoice is not in dispute");
        }

        this.status = ApStatus.UNPAID;
    }

    @Override
    public Long getId() {
        return id;
    }
}
