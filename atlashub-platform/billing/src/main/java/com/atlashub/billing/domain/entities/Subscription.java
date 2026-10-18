package com.atlashub.billing.domain.entities;

import com.atlashub.billing.domain.events.SubscriptionActivatedEvent;
import com.atlashub.billing.domain.events.SubscriptionCancelledEvent;
import com.atlashub.billing.domain.events.SubscriptionCreatedEvent;
import com.atlashub.billing.domain.events.SubscriptionRenewedEvent;
import com.atlashub.billing.domain.events.SubscriptionRestoredEvent;
import com.atlashub.billing.domain.events.SubscriptionSuspendedEvent;
import com.atlashub.billing.domain.exceptions.BillingDomainException;
import com.atlashub.billing.domain.exceptions.SubscriptionAlreadyActiveException;
import com.atlashub.billing.domain.exceptions.SubscriptionCancelledException;
import com.atlashub.billing.domain.exceptions.SubscriptionSuspendedException;
import com.atlashub.billing.domain.valueobject.SubscriptionStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
public class Subscription extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private Long pricingPlanId;
    private SubscriptionStatus status;
    private LocalDate currentPeriodStart;
    private LocalDate currentPeriodEnd;
    private LocalDate nextBillingDate;
    private final String billingCurrency;
    private final LocalDate trialEndsAt;
    private ZonedDateTime cancelledAt;
    private String cancelReason;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Subscription(
            Long id,
            Long organizationId,
            Long pricingPlanId,
            SubscriptionStatus status,
            LocalDate currentPeriodStart,
            LocalDate currentPeriodEnd,
            LocalDate nextBillingDate,
            String billingCurrency,
            LocalDate trialEndsAt,
            ZonedDateTime cancelledAt,
            String cancelReason,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.pricingPlanId = pricingPlanId;
        this.status = status;
        this.currentPeriodStart = currentPeriodStart;
        this.currentPeriodEnd = currentPeriodEnd;
        this.nextBillingDate = nextBillingDate;
        this.billingCurrency = billingCurrency;
        this.trialEndsAt = trialEndsAt;
        this.cancelledAt = cancelledAt;
        this.cancelReason = cancelReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Subscription create(
            Long id,
            Long organizationId,
            Long pricingPlanId,
            String billingCurrency,
            LocalDate trialEndsAt
    ) {
        if (id == null) {
            throw new BillingDomainException("Subscription ID cannot be null");
        }
        if (organizationId == null) {
            throw new BillingDomainException("Organization ID cannot be null");
        }
        if (billingCurrency == null || billingCurrency.isBlank()) {
            throw new BillingDomainException("Billing currency cannot be blank");
        }

        ZonedDateTime now = ZonedDateTime.now();
        Subscription subscription = new Subscription(
                id,
                organizationId,
                pricingPlanId,
                SubscriptionStatus.PENDING_PAYMENT,
                null,
                null,
                null,
                billingCurrency,
                trialEndsAt,
                null,
                null,
                now,
                now
        );

        subscription.registerEvent(SubscriptionCreatedEvent.of(
                id,
                organizationId,
                pricingPlanId,
                billingCurrency,
                trialEndsAt
        ));

        return subscription;
    }

    public void activate(LocalDate periodStart, LocalDate periodEnd) {
        if (this.status == SubscriptionStatus.ACTIVE) {
            throw new SubscriptionAlreadyActiveException("Subscription is already active for organization: " + organizationId);
        }
        if (this.status == SubscriptionStatus.CANCELLED) {
            throw new SubscriptionCancelledException("Cannot activate a cancelled subscription");
        }

        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriodStart = periodStart;
        this.currentPeriodEnd = periodEnd;
        this.nextBillingDate = periodEnd;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(SubscriptionActivatedEvent.of(
                id,
                organizationId,
                periodStart,
                periodEnd
        ));
    }

    public void suspend(String reason) {
        if (this.status == SubscriptionStatus.CANCELLED) {
            throw new SubscriptionCancelledException("Cannot suspend a cancelled subscription");
        }

        this.status = SubscriptionStatus.SUSPENDED;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(SubscriptionSuspendedEvent.of(
                id,
                organizationId,
                reason
        ));
    }

    public void restore() {
        if (this.status != SubscriptionStatus.SUSPENDED) {
            throw new SubscriptionSuspendedException("Subscription is not suspended; current status: " + this.status);
        }

        this.status = SubscriptionStatus.ACTIVE;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(SubscriptionRestoredEvent.of(
                id,
                organizationId
        ));
    }

    public void cancel(String reason, ZonedDateTime when) {
        this.status = SubscriptionStatus.CANCELLED;
        this.cancelledAt = when != null ? when : ZonedDateTime.now();
        this.cancelReason = reason;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(SubscriptionCancelledEvent.of(
                id,
                organizationId,
                reason
        ));
    }

    public void renew(LocalDate newPeriodStart, LocalDate newPeriodEnd) {
        if (this.status == SubscriptionStatus.CANCELLED) {
            throw new SubscriptionCancelledException("Cannot renew a cancelled subscription");
        }

        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriodStart = newPeriodStart;
        this.currentPeriodEnd = newPeriodEnd;
        this.nextBillingDate = newPeriodEnd;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(SubscriptionRenewedEvent.of(
                id,
                organizationId,
                newPeriodStart,
                newPeriodEnd
        ));
    }

    public void changePlan(Long newPricingPlanId) {
        if (newPricingPlanId == null) {
            throw new BillingDomainException("New pricing plan ID cannot be null");
        }
        if (this.status == SubscriptionStatus.CANCELLED) {
            throw new SubscriptionCancelledException("Cannot change plan on a cancelled subscription");
        }

        this.pricingPlanId = newPricingPlanId;
        this.updatedAt = ZonedDateTime.now();
    }

    public void markCompliancePending() {
        this.status = SubscriptionStatus.PENDING_COMPLIANCE;
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
