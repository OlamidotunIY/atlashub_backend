package com.atlashub.pay.mandates.domain.entities;

import com.atlashub.pay.mandates.domain.events.MandateChargeDueEvent;
import com.atlashub.pay.mandates.domain.events.MandateChargedEvent;
import com.atlashub.pay.mandates.domain.events.MandateExpiredEvent;
import com.atlashub.pay.mandates.domain.events.MandatePausedEvent;
import com.atlashub.pay.mandates.domain.events.MandateResumedEvent;
import com.atlashub.pay.mandates.domain.events.MandateRevokedEvent;
import com.atlashub.pay.mandates.domain.exceptions.ExpiredMandateException;
import com.atlashub.pay.mandates.domain.exceptions.InvalidMandateDetailsException;
import com.atlashub.pay.mandates.domain.exceptions.InvalidMandateStateException;
import com.atlashub.pay.mandates.domain.exceptions.MandatePausedException;
import com.atlashub.pay.mandates.domain.exceptions.MandateRevokedException;
import com.atlashub.pay.mandates.domain.valueobject.MandateFrequency;
import com.atlashub.pay.mandates.domain.valueobject.MandateStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
public class PaymentMandate extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String customerId;
    private final EmailAddress email;
    private final Money amount;
    private final MandateFrequency frequency;
    private final String authorizationCode;
    private MandateStatus status;
    private LocalDate nextChargeDate;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public PaymentMandate(Long id, Long organizationId, String customerId, EmailAddress email,
                          Money amount, MandateFrequency frequency, String authorizationCode,
                          MandateStatus status, LocalDate nextChargeDate,
                          ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.customerId = customerId;
        this.email = email;
        this.amount = amount;
        this.frequency = frequency;
        this.authorizationCode = authorizationCode;
        this.status = status;
        this.nextChargeDate = nextChargeDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PaymentMandate create(Long id, Long organizationId, String customerId,
                                        EmailAddress email, Money amount, MandateFrequency frequency,
                                        String authorizationCode, LocalDate firstChargeDate) {
        if (id == null) {
            throw new InvalidMandateDetailsException("Mandate ID cannot be null");
        }
        if (organizationId == null) {
            throw new InvalidMandateDetailsException("Organization ID cannot be null");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new InvalidMandateDetailsException("Customer ID cannot be empty");
        }
        if (email == null) {
            throw new InvalidMandateDetailsException("Customer email cannot be null");
        }
        if (amount == null) {
            throw new InvalidMandateDetailsException("Mandate amount cannot be null");
        }
        if (frequency == null) {
            throw new InvalidMandateDetailsException("Mandate frequency cannot be null");
        }
        if (authorizationCode == null || authorizationCode.isBlank()) {
            throw new InvalidMandateDetailsException("Authorization code cannot be empty");
        }
        if (firstChargeDate == null) {
            throw new InvalidMandateDetailsException("First charge date cannot be null");
        }

        return new PaymentMandate(
                id,
                organizationId,
                customerId,
                email,
                amount,
                frequency,
                authorizationCode,
                MandateStatus.ACTIVE,
                firstChargeDate,
                ZonedDateTime.now(),
                null
        );
    }

    public void pause() {
        if (status == MandateStatus.REVOKED) {
            throw new MandateRevokedException("Cannot pause a revoked mandate");
        }
        if (status == MandateStatus.EXPIRED) {
            throw new ExpiredMandateException("Cannot pause an expired mandate");
        }
        if (status != MandateStatus.ACTIVE) {
            throw new MandatePausedException("Payment mandate is already paused");
        }
        this.status = MandateStatus.PAUSED;
        this.updatedAt = ZonedDateTime.now();
        registerEvent(MandatePausedEvent.of(id, organizationId, customerId, email.value(), updatedAt));
    }

    public void resume() {
        if (status == MandateStatus.REVOKED) {
            throw new MandateRevokedException("Cannot resume a revoked mandate");
        }
        if (status == MandateStatus.EXPIRED) {
            throw new ExpiredMandateException("Cannot resume an expired mandate");
        }
        if (status != MandateStatus.PAUSED) {
            throw new InvalidMandateStateException("Payment mandate is not paused");
        }
        this.status = MandateStatus.ACTIVE;
        this.updatedAt = ZonedDateTime.now();
        registerEvent(MandateResumedEvent.of(id, organizationId, customerId, email.value(), updatedAt));
    }

    public void revoke() {
        if (status == MandateStatus.REVOKED) {
            return;
        }
        this.status = MandateStatus.REVOKED;
        this.updatedAt = ZonedDateTime.now();
        registerEvent(MandateRevokedEvent.of(
                id,
                organizationId,
                customerId,
                email.value(),
                amount.amount(),
                amount.currency().name(),
                frequency.name(),
                updatedAt
        ));
    }

    public void advanceNextChargeDate(MandateFrequency customFrequency) {
        if (status != MandateStatus.ACTIVE) {
            throw new InvalidMandateStateException("Cannot advance next charge date on a non-active mandate");
        }
        MandateFrequency freq = customFrequency != null ? customFrequency : this.frequency;
        this.nextChargeDate = switch (freq) {
            case DAILY -> this.nextChargeDate.plusDays(1);
            case WEEKLY -> this.nextChargeDate.plusWeeks(1);
            case MONTHLY -> this.nextChargeDate.plusMonths(1);
            case QUARTERLY -> this.nextChargeDate.plusMonths(3);
            case ANNUALLY -> this.nextChargeDate.plusYears(1);
        };
        this.updatedAt = ZonedDateTime.now();
    }

    public void advanceNextChargeDate() {
        advanceNextChargeDate(this.frequency);
    }

    public void triggerCharge(String chargeReference, ZonedDateTime dueAt) {
        if (status != MandateStatus.ACTIVE) {
            return;
        }
        registerEvent(MandateChargeDueEvent.of(
                id,
                organizationId,
                customerId,
                email.value(),
                chargeReference,
                amount.amount(),
                amount.currency().name(),
                authorizationCode,
                dueAt != null ? dueAt : ZonedDateTime.now()
        ));
        advanceNextChargeDate();
    }

    public void recordChargeSuccess(String chargeReference) {
        if (status == MandateStatus.ACTIVE) {
            registerEvent(MandateChargedEvent.of(
                    id,
                    organizationId,
                    customerId,
                    email.value(),
                    amount.amount(),
                    amount.currency().name(),
                    chargeReference,
                    ZonedDateTime.now()
            ));
        }
    }

    public void markExpired(String reason) {
        if (status == MandateStatus.EXPIRED) {
            return;
        }
        this.status = MandateStatus.EXPIRED;
        this.updatedAt = ZonedDateTime.now();
        registerEvent(MandateExpiredEvent.of(
                id,
                organizationId,
                customerId,
                email.value(),
                reason != null ? reason : "Payment mandate expired",
                updatedAt
        ));
    }

    public void markExpired() {
        markExpired(null);
    }

    @Override
    public Long getId() {
        return id;
    }
}
