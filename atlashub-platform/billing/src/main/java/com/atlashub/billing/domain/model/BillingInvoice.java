package com.atlashub.billing.domain.model;

import com.atlashub.billing.domain.valueobject.InvoiceStatus;
import com.atlashub.shared.domain.money.Money;
import com.atlashub.shared.domain.AggregateRoot;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.billing.domain.exception.BillingErrorCode;
import com.atlashub.billing.domain.events.BillingInvoicePaidEvent;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class BillingInvoice extends AggregateRoot<Long> {
    private final Long id;
    private final Long organizationProductId;
    private final Money amount;
    private InvoiceStatus status;
    private final ZonedDateTime dueDate;
    private ZonedDateTime paidAt;

    public BillingInvoice(Long id, Long organizationProductId, Money amount, InvoiceStatus status, ZonedDateTime dueDate, ZonedDateTime paidAt) {
        this.id = id;
        this.organizationProductId = organizationProductId;
        this.amount = amount;
        this.status = status;
        this.dueDate = dueDate;
        this.paidAt = paidAt;
    }

    public void markAsPaid(ZonedDateTime time) {
        if (this.status == InvoiceStatus.PAID) {
            throw new BusinessRuleException(
                    BillingErrorCode.INVOICE_ALREADY_PAID,
                    "Invoice is already paid"
            );
        }
        this.status = InvoiceStatus.PAID;
        this.paidAt = time;
        this.registerEvent(new BillingInvoicePaidEvent(
                UUID.randomUUID().toString(),
                String.valueOf(this.id),
                ZonedDateTime.now(),
                new BillingInvoicePaidEvent.Payload(this.id)
        ));
    }

    public void markAsFailed() {
        if (this.status == InvoiceStatus.PAID) {
            throw new BusinessRuleException(
                    BillingErrorCode.INVALID_SUBSCRIPTION_STATE,
                    "Cannot fail an already paid invoice"
            );
        }
        this.status = InvoiceStatus.FAILED;
    }

    public void requestWalletCharge(Long organizationId) {
        this.registerEvent(new com.atlashub.billing.domain.events.WalletChargeRequestedEvent(
                UUID.randomUUID().toString(),
                String.valueOf(this.id),
                ZonedDateTime.now(),
                new com.atlashub.billing.domain.events.WalletChargeRequestedEvent.Payload(
                        this.id,
                        organizationId,
                        this.amount.amount(),
                        this.amount.currency().name()
                )
        ));
    }

    public void requestExternalCharge(Long organizationId, String adminEmail, String redirectUrl) {
        this.registerEvent(new com.atlashub.billing.domain.events.ExternalChargeRequestedEvent(
                UUID.randomUUID().toString(),
                String.valueOf(this.id),
                ZonedDateTime.now(),
                new com.atlashub.billing.domain.events.ExternalChargeRequestedEvent.Payload(
                        this.id,
                        organizationId,
                        this.amount.amount(),
                        this.amount.currency().name(),
                        adminEmail,
                        redirectUrl
                )
        ));
    }

    @Override
    public Long getId() {
        return id;
    }
}
