package com.atlashub.billing.infrastructure.messaging.schedulers;

import com.atlashub.billing.application.command.SuspendSubscription.SuspendSubscriptionCommand;
import com.atlashub.billing.application.command.SuspendSubscription.SuspendSubscriptionHandler;
import com.atlashub.billing.domain.entities.BillingInvoice;
import com.atlashub.billing.domain.repositories.BillingInvoiceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class SubscriptionSuspensionScheduler {

    private final BillingInvoiceRepository billingInvoiceRepository;
    private final SuspendSubscriptionHandler suspendSubscriptionHandler;

    public SubscriptionSuspensionScheduler(
            BillingInvoiceRepository billingInvoiceRepository,
            SuspendSubscriptionHandler suspendSubscriptionHandler
    ) {
        this.billingInvoiceRepository = billingInvoiceRepository;
        this.suspendSubscriptionHandler = suspendSubscriptionHandler;
    }

    @Scheduled(cron = "0 5 8 * * *")
    public void processOverdueGraceExpirations() {
        LocalDate cutoff = LocalDate.now().minusDays(7);
        List<BillingInvoice> overdueInvoices = billingInvoiceRepository.findOverdueInvoicesOlderThan(cutoff);

        for (BillingInvoice invoice : overdueInvoices) {
            if (invoice.getSubscriptionId() != null) {
                suspendSubscriptionHandler.execute(new SuspendSubscriptionCommand(
                        invoice.getSubscriptionId(),
                        "Invoice " + invoice.getInvoiceNumber() + " unpaid past 7-day grace period"
                ));
            }
        }
    }
}
