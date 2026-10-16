package com.atlashub.billing.domain.entities;

import com.atlashub.billing.domain.events.InvoiceIssuedEvent;
import com.atlashub.billing.domain.events.InvoiceOverdueEvent;
import com.atlashub.billing.domain.events.InvoicePaidEvent;
import com.atlashub.billing.domain.events.InvoiceVoidedEvent;
import com.atlashub.billing.domain.exceptions.BillingDomainException;
import com.atlashub.billing.domain.exceptions.InvoiceAlreadyPaidException;
import com.atlashub.billing.domain.exceptions.InvoiceVoidedException;
import com.atlashub.billing.domain.valueobject.InvoiceStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
public class BillingInvoice extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long subscriptionId;
    private final String invoiceNumber;
    private final String productCode;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final BigDecimal subtotal;
    private final BigDecimal tax;
    private final BigDecimal totalAmount;
    private final String currency;
    private InvoiceStatus status;
    private final LocalDate dueDate;
    private ZonedDateTime issuedAt;
    private ZonedDateTime paidAt;
    private String paymentReference;
    private String paymentCheckoutUrl;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public BillingInvoice(
            Long id,
            Long organizationId,
            Long subscriptionId,
            String invoiceNumber,
            String productCode,
            LocalDate periodStart,
            LocalDate periodEnd,
            BigDecimal subtotal,
            BigDecimal tax,
            BigDecimal totalAmount,
            String currency,
            InvoiceStatus status,
            LocalDate dueDate,
            ZonedDateTime issuedAt,
            ZonedDateTime paidAt,
            String paymentReference,
            String paymentCheckoutUrl,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.subscriptionId = subscriptionId;
        this.invoiceNumber = invoiceNumber;
        this.productCode = productCode;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.subtotal = subtotal;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.status = status;
        this.dueDate = dueDate;
        this.issuedAt = issuedAt;
        this.paidAt = paidAt;
        this.paymentReference = paymentReference;
        this.paymentCheckoutUrl = paymentCheckoutUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static BillingInvoice create(
            Long id,
            Long organizationId,
            Long subscriptionId,
            String invoiceNumber,
            String productCode,
            LocalDate periodStart,
            LocalDate periodEnd,
            BigDecimal subtotal,
            BigDecimal tax,
            String currency,
            LocalDate dueDate
    ) {
        if (id == null) {
            throw new BillingDomainException("Invoice ID cannot be null");
        }
        if (organizationId == null) {
            throw new BillingDomainException("Organization ID cannot be null");
        }
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            throw new BillingDomainException("Invoice number cannot be blank");
        }
        if (productCode == null || productCode.isBlank()) {
            throw new BillingDomainException("Product code cannot be blank");
        }
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new BillingDomainException("Subtotal cannot be negative");
        }
        BigDecimal actualTax = tax != null ? tax : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(actualTax);

        ZonedDateTime now = ZonedDateTime.now();
        return new BillingInvoice(
                id,
                organizationId,
                subscriptionId,
                invoiceNumber,
                productCode,
                periodStart,
                periodEnd,
                subtotal,
                actualTax,
                total,
                currency != null ? currency : "NGN",
                InvoiceStatus.DRAFT,
                dueDate != null ? dueDate : LocalDate.now().plusDays(7),
                null,
                null,
                null,
                null,
                now,
                now
        );
    }

    public void issue() {
        if (this.status == InvoiceStatus.VOID) {
            throw new InvoiceVoidedException("Cannot issue a voided invoice: " + invoiceNumber);
        }
        if (this.status == InvoiceStatus.PAID) {
            throw new InvoiceAlreadyPaidException("Cannot issue an already paid invoice: " + invoiceNumber);
        }

        this.status = InvoiceStatus.ISSUED;
        this.issuedAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();

        registerEvent(InvoiceIssuedEvent.of(
                id,
                organizationId,
                subscriptionId,
                invoiceNumber,
                productCode,
                totalAmount,
                currency,
                dueDate
        ));
    }

    public void markPaid(String paymentReference, ZonedDateTime paidAt) {
        if (this.status == InvoiceStatus.PAID) {
            throw new InvoiceAlreadyPaidException("Invoice is already marked as paid: " + invoiceNumber);
        }
        if (this.status == InvoiceStatus.VOID) {
            throw new InvoiceVoidedException("Cannot mark voided invoice as paid: " + invoiceNumber);
        }

        this.status = InvoiceStatus.PAID;
        this.paymentReference = paymentReference;
        this.paidAt = paidAt != null ? paidAt : ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();

        registerEvent(InvoicePaidEvent.of(
                id,
                organizationId,
                subscriptionId,
                invoiceNumber,
                paymentReference,
                this.paidAt
        ));
    }

    public void markOverdue() {
        if (this.status == InvoiceStatus.PAID || this.status == InvoiceStatus.VOID || this.status == InvoiceStatus.OVERDUE) {
            return;
        }

        this.status = InvoiceStatus.OVERDUE;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(InvoiceOverdueEvent.of(
                id,
                organizationId,
                subscriptionId,
                invoiceNumber,
                dueDate
        ));
    }

    public void voidInvoice(String reason) {
        if (this.status == InvoiceStatus.PAID) {
            throw new InvoiceAlreadyPaidException("Cannot void an already paid invoice: " + invoiceNumber);
        }

        this.status = InvoiceStatus.VOID;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(InvoiceVoidedEvent.of(
                id,
                organizationId,
                subscriptionId,
                invoiceNumber,
                reason
        ));
    }

    public void updatePaymentCheckoutUrl(String paymentCheckoutUrl) {
        this.paymentCheckoutUrl = paymentCheckoutUrl;
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
