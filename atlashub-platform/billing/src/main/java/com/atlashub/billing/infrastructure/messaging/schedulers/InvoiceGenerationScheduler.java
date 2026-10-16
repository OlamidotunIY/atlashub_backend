package com.atlashub.billing.infrastructure.messaging.schedulers;

import com.atlashub.billing.application.command.RenewSubscription.RenewSubscriptionCommand;
import com.atlashub.billing.application.command.RenewSubscription.RenewSubscriptionHandler;
import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.repositories.SubscriptionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class InvoiceGenerationScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final RenewSubscriptionHandler renewSubscriptionHandler;

    public InvoiceGenerationScheduler(
            SubscriptionRepository subscriptionRepository,
            RenewSubscriptionHandler renewSubscriptionHandler
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.renewSubscriptionHandler = renewSubscriptionHandler;
    }

    @Scheduled(cron = "0 5 0 1 * *")
    public void processMonthlyRenewals() {
        List<Subscription> dueSubscriptions = subscriptionRepository.findActiveSubscriptionsDueForRenewal(LocalDate.now());
        for (Subscription sub : dueSubscriptions) {
            renewSubscriptionHandler.execute(new RenewSubscriptionCommand(sub.getId()));
        }
    }
}
