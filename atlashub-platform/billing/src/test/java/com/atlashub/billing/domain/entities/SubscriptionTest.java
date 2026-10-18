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
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SubscriptionTest {

    @Test
    void create_validSubscription_setsPendingPaymentAndRegistersEvent() {
        Subscription subscription = Subscription.create(
                100L,
                50L,
                1L,
                "NGN",
                LocalDate.now().plusDays(30)
        );

        assertEquals(100L, subscription.getId());
        assertEquals(50L, subscription.getOrganizationId());
        assertEquals(SubscriptionStatus.PENDING_PAYMENT, subscription.getStatus());
        assertEquals("NGN", subscription.getBillingCurrency());
        assertNotNull(subscription.getTrialEndsAt());

        assertEquals(1, subscription.pullDomainEvents().size());
    }

    @Test
    void create_missingRequiredFields_throwsException() {
        assertThrows(BillingDomainException.class, () -> Subscription.create(
                null, 50L, 1L, "NGN", LocalDate.now().plusDays(30)
        ));

        assertThrows(BillingDomainException.class, () -> Subscription.create(
                100L, null, 1L, "NGN", LocalDate.now().plusDays(30)
        ));

        assertThrows(BillingDomainException.class, () -> Subscription.create(
                100L, 50L, 1L, "", LocalDate.now().plusDays(30)
        ));
    }

    @Test
    void activate_transitionsToActive_andRegistersEvent() {
        Subscription subscription = Subscription.create(
                100L, 50L, 1L, "NGN", LocalDate.now().plusDays(30)
        );
        subscription.pullDomainEvents();

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(1);
        subscription.activate(start, end);

        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        assertEquals(start, subscription.getCurrentPeriodStart());
        assertEquals(end, subscription.getCurrentPeriodEnd());
        assertEquals(end, subscription.getNextBillingDate());

        assertEquals(1, subscription.pullDomainEvents().size());
    }

    @Test
    void activate_alreadyActive_throwsException() {
        Subscription subscription = Subscription.create(
                100L, 50L, 1L, "NGN", LocalDate.now().plusDays(30)
        );
        subscription.activate(LocalDate.now(), LocalDate.now().plusMonths(1));

        assertThrows(SubscriptionAlreadyActiveException.class, () ->
                subscription.activate(LocalDate.now(), LocalDate.now().plusMonths(1))
        );
    }

    @Test
    void suspend_and_restore_success() {
        Subscription subscription = Subscription.create(
                100L, 50L, 1L, "NGN", LocalDate.now().plusDays(30)
        );
        subscription.activate(LocalDate.now(), LocalDate.now().plusMonths(1));

        subscription.suspend("Payment overdue");
        assertEquals(SubscriptionStatus.SUSPENDED, subscription.getStatus());

        subscription.restore();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
    }

    @Test
    void restore_whenNotSuspended_throwsException() {
        Subscription subscription = Subscription.create(
                100L, 50L, 1L, "NGN", LocalDate.now().plusDays(30)
        );

        assertThrows(SubscriptionSuspendedException.class, subscription::restore);
    }

    @Test
    void cancel_transitionsToCancelled() {
        Subscription subscription = Subscription.create(
                100L, 50L, 1L, "NGN", LocalDate.now().plusDays(30)
        );
        subscription.cancel("User cancellation", ZonedDateTime.now());

        assertEquals(SubscriptionStatus.CANCELLED, subscription.getStatus());
        assertEquals("User cancellation", subscription.getCancelReason());
        assertNotNull(subscription.getCancelledAt());

        assertThrows(SubscriptionCancelledException.class, () ->
                subscription.renew(LocalDate.now(), LocalDate.now().plusMonths(1))
        );
    }

    @Test
    void renew_advancesBillingPeriod() {
        Subscription subscription = Subscription.create(
                100L, 50L, 1L, "NGN", LocalDate.now().plusDays(30)
        );
        subscription.activate(LocalDate.now(), LocalDate.now().plusMonths(1));

        LocalDate newStart = LocalDate.now().plusMonths(1);
        LocalDate newEnd = newStart.plusMonths(1);
        subscription.renew(newStart, newEnd);

        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        assertEquals(newStart, subscription.getCurrentPeriodStart());
        assertEquals(newEnd, subscription.getCurrentPeriodEnd());
    }

    @Test
    void changePlan_updatesPricingPlanId() {
        Subscription subscription = Subscription.create(
                100L, 50L, 1L, "NGN", LocalDate.now().plusDays(30)
        );
        subscription.changePlan(2L);

        assertEquals(2L, subscription.getPricingPlanId());
    }
}
