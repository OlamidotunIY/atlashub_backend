package com.atlashub.billing.infrastructure.persistence.entities;

import com.atlashub.billing.domain.valueobject.InvoiceStatus;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "billing_invoices",
        indexes = {
                @Index(name = "idx_invoice_number", columnList = "invoice_number", unique = true),
                @Index(name = "idx_invoice_org_status", columnList = "organization_id, status"),
                @Index(name = "idx_invoice_payment_ref", columnList = "payment_reference"),
                @Index(name = "idx_invoice_status_due", columnList = "status, due_date")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BillingInvoiceJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @Column(name = "product_code", nullable = false)
    private String productCode;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(name = "tax", nullable = false, precision = 19, scale = 4)
    private BigDecimal tax;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "issued_at")
    private ZonedDateTime issuedAt;

    @Column(name = "paid_at")
    private ZonedDateTime paidAt;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "payment_checkout_url")
    private String paymentCheckoutUrl;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
