package com.atlashub.billing.application.query.GetSubscription;

import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.exceptions.SubscriptionNotFoundException;
import com.atlashub.billing.domain.repositories.SubscriptionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

@Component
public class GetSubscriptionHandler extends Query<GetSubscriptionQuery, SubscriptionResult> {

    private final SubscriptionRepository subscriptionRepository;

    public GetSubscriptionHandler(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public SubscriptionResult execute(GetSubscriptionQuery query) {
        Subscription subscription = subscriptionRepository.findByOrganizationId(query.organizationId())
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found for organization: " + query.organizationId()));

        return new SubscriptionResult(
                subscription.getId(),
                subscription.getOrganizationId(),
                subscription.getPricingPlanId(),
                subscription.getStatus().name(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                subscription.getNextBillingDate(),
                subscription.getBillingCurrency(),
                subscription.getTrialEndsAt(),
                subscription.getCancelledAt(),
                subscription.getCancelReason(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }
}
