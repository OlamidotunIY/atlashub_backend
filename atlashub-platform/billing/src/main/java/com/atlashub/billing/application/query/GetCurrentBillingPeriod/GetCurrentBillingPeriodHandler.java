package com.atlashub.billing.application.query.GetCurrentBillingPeriod;

import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.exceptions.SubscriptionNotFoundException;
import com.atlashub.billing.domain.repositories.SubscriptionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

@Component
public class GetCurrentBillingPeriodHandler extends Query<GetCurrentBillingPeriodQuery, BillingPeriodResult> {

    private final SubscriptionRepository subscriptionRepository;

    public GetCurrentBillingPeriodHandler(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public BillingPeriodResult execute(GetCurrentBillingPeriodQuery query) {
        Subscription subscription = subscriptionRepository.findById(query.subscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found: " + query.subscriptionId()));

        return new BillingPeriodResult(
                subscription.getId(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                subscription.getNextBillingDate(),
                subscription.getStatus().name()
        );
    }
}
