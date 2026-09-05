package com.atlashub.charges.domain.model;

import com.atlashub.charges.domain.event.ExternalChargeInitiatedEvent;
import com.atlashub.charges.domain.event.ExternalPaymentFailedEvent;
import com.atlashub.charges.domain.event.ExternalPaymentSuccessfulEvent;
import com.atlashub.charges.domain.valueobject.ChargePurpose;
import com.atlashub.charges.domain.valueobject.ChargeStatus;
import com.atlashub.shared.domain.AggregateRoot;
import com.atlashub.shared.domain.money.Money;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class ExternalCharge extends AggregateRoot<Long> {
    private final Long id;
    private final Long purposeId;
    private final Long organizationId;
    private final String reference;
    private final String checkoutUrl;
    private final Money amount;
    private final ChargePurpose purpose;
    private ChargeStatus status;
    private ZonedDateTime completedAt;
    private final ZonedDateTime createdAt;

    public ExternalCharge(Long id, Long purposeId, Long organizationId, String reference, String checkoutUrl, Money amount, ChargePurpose purpose, ChargeStatus status, ZonedDateTime completedAt, ZonedDateTime createdAt) {
        this.id = id;
        this.purposeId = purposeId;
        this.organizationId = organizationId;
        this.reference = reference;
        this.checkoutUrl = checkoutUrl;
        this.amount = amount;
        this.purpose = purpose;
        this.status = status;
        this.completedAt = completedAt;
        this.createdAt = createdAt != null ? createdAt : ZonedDateTime.now();
    }

    public static ExternalCharge initiate(Long id, Long purposeId, Long organizationId, String reference, String checkoutUrl, Money amount, ChargePurpose purpose) {
        ExternalCharge charge = new ExternalCharge(id, purposeId, organizationId, reference, checkoutUrl, amount, purpose, ChargeStatus.INITIATED, null, ZonedDateTime.now());
        charge.registerEvent(new ExternalChargeInitiatedEvent(
                UUID.randomUUID().toString(),
                String.valueOf(charge.getId()),
                ZonedDateTime.now(),
                new ExternalChargeInitiatedEvent.Payload(charge.purposeId, charge.reference, charge.checkoutUrl)
        ));
        return charge;
    }

    public void markSuccessful() {
        if (this.status == ChargeStatus.SUCCESSFUL) return;
        this.status = ChargeStatus.SUCCESSFUL;
        this.completedAt = ZonedDateTime.now();
        registerEvent(new ExternalPaymentSuccessfulEvent(
                UUID.randomUUID().toString(),
                String.valueOf(this.id),
                ZonedDateTime.now(),
                new ExternalPaymentSuccessfulEvent.Payload(this.reference, this.purpose, this.purposeId)
        ));
    }

    public void markFailed(String reason) {
        if (this.status == ChargeStatus.FAILED) return;
        this.status = ChargeStatus.FAILED;
        this.completedAt = ZonedDateTime.now();
        registerEvent(new ExternalPaymentFailedEvent(
                UUID.randomUUID().toString(),
                String.valueOf(this.id),
                ZonedDateTime.now(),
                new ExternalPaymentFailedEvent.Payload(this.reference, this.purpose, this.purposeId, reason)
        ));
    }
}
