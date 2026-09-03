package com.atlashub.ledger.domain.model;

import com.atlashub.ledger.domain.event.WalletChargeFailedEvent;
import com.atlashub.ledger.domain.event.WalletChargeSuccessfulEvent;
import com.atlashub.ledger.domain.valueobject.WalletChargeStatus;
import com.atlashub.shared.domain.AggregateRoot;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public class WalletCharge extends AggregateRoot<Long> {
    private final Long id;
    private final Long invoiceId;
    private final Long organizationId;
    private final BigDecimal amount;
    private final String currency;
    private final String ledgerTransactionId;
    private WalletChargeStatus status;
    private String failureReason;

    // Creation for mapping
    public WalletCharge(Long id, Long invoiceId, Long organizationId, BigDecimal amount, String currency, String ledgerTransactionId, WalletChargeStatus status, String failureReason) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.organizationId = organizationId;
        this.amount = amount;
        this.currency = currency;
        this.ledgerTransactionId = ledgerTransactionId;
        this.status = status;
        this.failureReason = failureReason;
    }

    public static WalletCharge create(Long id, Long invoiceId, Long organizationId, BigDecimal amount, String currency, String ledgerTransactionId) {
        WalletCharge charge = new WalletCharge(id, invoiceId, organizationId, amount, currency, ledgerTransactionId, WalletChargeStatus.SUCCESSFUL, null);
        charge.registerEvent(new WalletChargeSuccessfulEvent(
                UUID.randomUUID().toString(),
                String.valueOf(charge.getId()),
                ZonedDateTime.now(),
                new WalletChargeSuccessfulEvent.Payload(invoiceId, charge.getId())
        ));
        return charge;
    }

    public static WalletCharge fail(Long id, Long invoiceId, Long organizationId, BigDecimal amount, String currency, String reason) {
        WalletCharge charge = new WalletCharge(id, invoiceId, organizationId, amount, currency, null, WalletChargeStatus.FAILED, reason);
        charge.registerEvent(new WalletChargeFailedEvent(
                UUID.randomUUID().toString(),
                String.valueOf(charge.getId()),
                ZonedDateTime.now(),
                new WalletChargeFailedEvent.Payload(invoiceId, reason)
        ));
        return charge;
    }

    @Override
    public Long getId() {
        return id;
    }
    public Long getInvoiceId() { return invoiceId; }
    public Long getOrganizationId() { return organizationId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getLedgerTransactionId() { return ledgerTransactionId; }
    public WalletChargeStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
}
