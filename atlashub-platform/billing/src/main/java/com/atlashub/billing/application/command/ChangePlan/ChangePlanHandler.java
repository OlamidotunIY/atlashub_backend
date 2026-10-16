package com.atlashub.billing.application.command.ChangePlan;

import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.exceptions.SubscriptionNotFoundException;
import com.atlashub.billing.domain.repositories.SubscriptionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class ChangePlanHandler extends Command<ChangePlanCommand, ChangePlanResult> {

    private final SubscriptionRepository subscriptionRepository;

    public ChangePlanHandler(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public ChangePlanResult execute(ChangePlanCommand command) {
        Subscription subscription = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found: " + command.subscriptionId()));

        subscription.changePlan(command.newPricingPlanId());
        subscriptionRepository.save(subscription);

        return new ChangePlanResult(
                subscription.getId(),
                subscription.getPricingPlanId(),
                subscription.getStatus().name()
        );
    }
}
