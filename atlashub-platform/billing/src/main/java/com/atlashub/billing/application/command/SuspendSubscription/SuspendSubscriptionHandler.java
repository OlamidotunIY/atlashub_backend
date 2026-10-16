package com.atlashub.billing.application.command.SuspendSubscription;

import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.exceptions.SubscriptionNotFoundException;
import com.atlashub.billing.domain.repositories.SubscriptionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class SuspendSubscriptionHandler extends Command<SuspendSubscriptionCommand, SuspendSubscriptionResult> {

    private final SubscriptionRepository subscriptionRepository;

    public SuspendSubscriptionHandler(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public SuspendSubscriptionResult execute(SuspendSubscriptionCommand command) {
        Subscription subscription = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found: " + command.subscriptionId()));

        subscription.suspend(command.reason() != null ? command.reason() : "Invoice payment overdue beyond grace period");
        subscriptionRepository.save(subscription);

        return new SuspendSubscriptionResult(subscription.getId(), subscription.getStatus().name());
    }
}
