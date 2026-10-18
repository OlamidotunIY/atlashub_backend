package com.atlashub.billing.application.command.ActivateSubscription;

import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.exceptions.SubscriptionNotFoundException;
import com.atlashub.billing.domain.repositories.SubscriptionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ActivateSubscriptionHandler extends Command<ActivateSubscriptionCommand, ActivateSubscriptionResult> {

    private final SubscriptionRepository subscriptionRepository;

    public ActivateSubscriptionHandler(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public ActivateSubscriptionResult execute(ActivateSubscriptionCommand command) {
        Subscription subscription = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found: " + command.subscriptionId()));

        LocalDate start = command.periodStart() != null ? command.periodStart() : LocalDate.now();
        LocalDate end = command.periodEnd() != null ? command.periodEnd() : start.plusMonths(1);

        subscription.activate(start, end);
        subscriptionRepository.save(subscription);

        return new ActivateSubscriptionResult(subscription.getId(), subscription.getStatus().name());
    }
}
