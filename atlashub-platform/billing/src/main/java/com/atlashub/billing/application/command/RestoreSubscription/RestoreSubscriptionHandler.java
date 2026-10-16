package com.atlashub.billing.application.command.RestoreSubscription;

import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.exceptions.SubscriptionNotFoundException;
import com.atlashub.billing.domain.repositories.SubscriptionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class RestoreSubscriptionHandler extends Command<RestoreSubscriptionCommand, RestoreSubscriptionResult> {

    private final SubscriptionRepository subscriptionRepository;

    public RestoreSubscriptionHandler(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public RestoreSubscriptionResult execute(RestoreSubscriptionCommand command) {
        Subscription subscription = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found: " + command.subscriptionId()));

        subscription.restore();
        subscriptionRepository.save(subscription);

        return new RestoreSubscriptionResult(subscription.getId(), subscription.getStatus().name());
    }
}
