package com.atlashub.billing.domain.model;

import com.atlashub.billing.domain.valueobject.InvoiceStatus;
import com.atlashub.shared.money.Money;
import com.atlashub.shared.domain.AggregateRoot;
import com.atlashub.shared.exception.BusinessRuleException;
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

    @Override
    public Long getId() {
        return id;
    }
}
