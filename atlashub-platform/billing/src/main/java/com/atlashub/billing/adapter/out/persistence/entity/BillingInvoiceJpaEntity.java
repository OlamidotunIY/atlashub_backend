package com.atlashub.billing.adapter.out.persistence.entity;

import com.atlashub.billing.domain.valueobject.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "billing_invoices", indexes = {
    @Index(name = "idx_invoice_org_product", columnList = "organization_product_id")
})
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BillingInvoiceJpaEntity {
    @Id
    @Column(nullable = false)
    private Long id;

    @Column(name = "organization_product_id", nullable = false)
    private Long organizationProductId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status;

    @Column(name = "due_date", nullable = false)
    private ZonedDateTime dueDate;

    @Setter
    @Column(name = "paid_at")
    private ZonedDateTime paidAt;

    @Version
    private Long version;
}
