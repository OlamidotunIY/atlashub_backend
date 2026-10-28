package com.atlashub.billing.application.command.InitializeBusinessEntitlements;

import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.repositories.SubscriptionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Initializes baseline platform access after an organization event, without issuing an invoice. */
@Component
public class InitializeBusinessEntitlementsHandler {
    private final SubscriptionRepository subscriptions;

    public InitializeBusinessEntitlementsHandler(SubscriptionRepository subscriptions) {
        this.subscriptions = subscriptions;
    }

    @Transactional
    public void execute(Long organizationId, String currency) {
        if (subscriptions.findByOrganizationId(organizationId).isPresent()) return;
        LocalDate today = LocalDate.now();
        Subscription subscription = Subscription.create(subscriptions.nextIdentity(), organizationId,
                1L, currency, today.plusDays(30));
        subscription.activate(today, today.plusDays(30));
        subscriptions.save(subscription);
    }
}
