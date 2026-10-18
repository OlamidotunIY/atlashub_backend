package com.atlashub.billing.application.command.CancelSubscription;

import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.exceptions.SubscriptionNotFoundException;
import com.atlashub.billing.domain.repositories.SubscriptionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class CancelSubscriptionHandler extends Command<CancelSubscriptionCommand, CancelSubscriptionResult> {

    private final SubscriptionRepository subscriptionRepository;

    public CancelSubscriptionHandler(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public CancelSubscriptionResult execute(CancelSubscriptionCommand command) {
        Subscription subscription = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found: " + command.subscriptionId()));

        subscription.cancel(command.reason() != null ? command.reason() : "Cancelled by user request", ZonedDateTime.now());
        subscriptionRepository.save(subscription);

        return new CancelSubscriptionResult(subscription.getId(), subscription.getStatus().name());
    }
}
