package com.atlashub.billing.domain.model;

import com.atlashub.billing.domain.events.ProductSubscribedEvent;
import com.atlashub.billing.domain.events.SubscriptionCanceledEvent;
import com.atlashub.billing.domain.events.SubscriptionRenewedEvent;
import com.atlashub.billing.domain.events.SubscriptionSuspendedEvent;
import com.atlashub.billing.domain.exception.BillingErrorCode;
import com.atlashub.billing.domain.valueobject.SubscriptionStatus;
import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.shared.domain.AggregateRoot;
import com.atlashub.shared.exception.BusinessRuleException;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class OrganizationProduct extends AggregateRoot<Long> {
    private final Long id;
    private final Long organizationId;
    private final Long productId;
    private SubscriptionStatus status;
    private final BillingCycle cycle;
    private ZonedDateTime currentPeriodStart;
    private ZonedDateTime currentPeriodEnd;
    private ZonedDateTime canceledAt;

    public OrganizationProduct(Long id, Long organizationId, Long productId, SubscriptionStatus status, BillingCycle cycle, ZonedDateTime currentPeriodStart, ZonedDateTime currentPeriodEnd, ZonedDateTime canceledAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.productId = productId;
        this.status = status;
        this.cycle = cycle;
        this.currentPeriodStart = currentPeriodStart;
        this.currentPeriodEnd = currentPeriodEnd;
        this.canceledAt = canceledAt;
    }

    public static OrganizationProduct create(Long id, Long organizationId, Long productId, SubscriptionStatus status, BillingCycle cycle) {
        OrganizationProduct product = new OrganizationProduct(id, organizationId, productId, status, cycle, null, null, null);
        product.registerEvent(new ProductSubscribedEvent(
                UUID.randomUUID().toString(),
                String.valueOf(id),
                ZonedDateTime.now(),
                new ProductSubscribedEvent.Payload(organizationId, productId)
        ));
        return product;
    }

    public void activate(ZonedDateTime start, ZonedDateTime end) {
        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriodStart = start;
        this.currentPeriodEnd = end;
        this.canceledAt = null;
    }

    public void suspend(String reason) {
        if (this.status == SubscriptionStatus.CANCELED) {
            throw new BusinessRuleException(
                    BillingErrorCode.INVALID_SUBSCRIPTION_STATE,
                    "Cannot suspend a canceled subscription"
            );
        }
        this.status = SubscriptionStatus.SUSPENDED;
        this.registerEvent(new SubscriptionSuspendedEvent(
                UUID.randomUUID().toString(),
                String.valueOf(this.id),
                ZonedDateTime.now(),
                new SubscriptionSuspendedEvent.Payload(reason)
        ));
    }

    public void cancel(String reason) {
        if (this.status == SubscriptionStatus.CANCELED) {
            throw new BusinessRuleException(
                    BillingErrorCode.INVALID_SUBSCRIPTION_STATE,
                    "Subscription is already canceled"
            );
        }
        this.status = SubscriptionStatus.CANCELED;
        this.canceledAt = ZonedDateTime.now();
        this.registerEvent(new SubscriptionCanceledEvent(
                UUID.randomUUID().toString(),
                String.valueOf(this.id),
                ZonedDateTime.now(),
                new SubscriptionCanceledEvent.Payload(reason)
        ));
    }

    public void renew(ZonedDateTime newEnd) {
        if (this.status == SubscriptionStatus.CANCELED) {
            throw new BusinessRuleException(
                    BillingErrorCode.INVALID_SUBSCRIPTION_STATE,
                    "Cannot renew a canceled subscription"
            );
        }
        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriodEnd = newEnd;
        this.registerEvent(new SubscriptionRenewedEvent(
                UUID.randomUUID().toString(),
                String.valueOf(this.id),
                ZonedDateTime.now(),
                new SubscriptionRenewedEvent.Payload(newEnd)
        ));
    }

    public boolean isExpired() {
        if (this.currentPeriodEnd == null) return true;
        return ZonedDateTime.now().isAfter(this.currentPeriodEnd);
    }

    @Override
    public Long getId() {
        return id;
    }
}
