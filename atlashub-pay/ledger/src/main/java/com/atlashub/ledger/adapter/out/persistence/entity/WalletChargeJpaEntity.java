package com.atlashub.ledger.adapter.out.persistence.entity;

import com.atlashub.ledger.domain.valueobject.WalletChargeStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "wallet_charges", indexes = {
    @Index(name = "idx_wallet_charge_invoice", columnList = "invoice_id")
})
public class WalletChargeJpaEntity {
    @Id
    @Column(nullable = false)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "ledger_transaction_id")
    private String ledgerTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WalletChargeStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    protected WalletChargeJpaEntity() {}

    public WalletChargeJpaEntity(Long id, Long invoiceId, Long organizationId, BigDecimal amount, String currency, String ledgerTransactionId, WalletChargeStatus status, String failureReason) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.organizationId = organizationId;
        this.amount = amount;
        this.currency = currency;
        this.ledgerTransactionId = ledgerTransactionId;
        this.status = status;
        this.failureReason = failureReason;
    }

    public Long getId() { return id; }
    public Long getInvoiceId() { return invoiceId; }
    public Long getOrganizationId() { return organizationId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getLedgerTransactionId() { return ledgerTransactionId; }
    public WalletChargeStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
}
