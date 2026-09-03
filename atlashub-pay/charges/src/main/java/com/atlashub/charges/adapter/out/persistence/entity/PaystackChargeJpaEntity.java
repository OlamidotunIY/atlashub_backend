package com.atlashub.charges.adapter.out.persistence.entity;

import com.atlashub.charges.domain.valueobject.ChargePurpose;
import com.atlashub.charges.domain.valueobject.ChargeStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "paystack_charges", indexes = {
    @Index(name = "idx_charge_reference", columnList = "reference", unique = true),
    @Index(name = "idx_charge_invoice_id", columnList = "invoice_id"),
    @Index(name = "idx_charge_organization_id", columnList = "organization_id")
})
public class PaystackChargeJpaEntity {

    @Id
    @Column(nullable = false)
    private Long id;

    @Column(name = "purpose_id", nullable = false)
    private Long purposeId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "reference", nullable = false, unique = true)
    private String reference;

    @Column(name = "checkout_url", nullable = false)
    private String checkoutUrl;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private ChargePurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChargeStatus status;

    @Column(name = "completed_at")
    private ZonedDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Version
    private Long version;

    protected PaystackChargeJpaEntity() {}

    public PaystackChargeJpaEntity(Long id, Long invoiceId, Long organizationId, String reference, String checkoutUrl, BigDecimal amount, String currency, ChargePurpose purpose, ChargeStatus status, ZonedDateTime completedAt, ZonedDateTime createdAt, Long version) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.organizationId = organizationId;
        this.reference = reference;
        this.checkoutUrl = checkoutUrl;
        this.amount = amount;
        this.currency = currency;
        this.purpose = purpose;
        this.status = status;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.version = version;
    }

    public Long getId() { return id; }
    public Long getInvoiceId() { return invoiceId; }
    public Long getOrganizationId() { return organizationId; }
    public String getReference() { return reference; }
    public String getCheckoutUrl() { return checkoutUrl; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public ChargePurpose getPurpose() { return purpose; }
    public ChargeStatus getStatus() { return status; }
    public ZonedDateTime getCompletedAt() { return completedAt; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public Long getVersion() { return version; }

    public void setStatus(ChargeStatus status) { this.status = status; }
    public void setCompletedAt(ZonedDateTime completedAt) { this.completedAt = completedAt; }
}
