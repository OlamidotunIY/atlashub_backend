package com.atlashub.billing.application.command.MarkInvoicePaid;

import com.atlashub.billing.domain.entities.BillingInvoice;
import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.exceptions.InvoiceNotFoundException;
import com.atlashub.billing.domain.repositories.BillingInvoiceRepository;
import com.atlashub.billing.domain.repositories.SubscriptionRepository;
import com.atlashub.billing.domain.valueobject.SubscriptionStatus;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

@Component
public class MarkInvoicePaidHandler extends Command<MarkInvoicePaidCommand, MarkInvoicePaidResult> {

    private final BillingInvoiceRepository billingInvoiceRepository;
    private final SubscriptionRepository subscriptionRepository;

    public MarkInvoicePaidHandler(
            BillingInvoiceRepository billingInvoiceRepository,
            SubscriptionRepository subscriptionRepository
    ) {
        this.billingInvoiceRepository = billingInvoiceRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public MarkInvoicePaidResult execute(MarkInvoicePaidCommand command) {
        BillingInvoice invoice = billingInvoiceRepository.findById(command.invoiceId())
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found: " + command.invoiceId()));

        invoice.markPaid(command.paymentReference(), command.paidAt() != null ? command.paidAt() : ZonedDateTime.now());
        billingInvoiceRepository.save(invoice);

        // If subscription is linked, activate or restore if appropriate
        if (invoice.getSubscriptionId() != null) {
            Optional<Subscription> subscriptionOpt = subscriptionRepository.findById(invoice.getSubscriptionId());
            if (subscriptionOpt.isPresent()) {
                Subscription subscription = subscriptionOpt.get();
                if (subscription.getStatus() == SubscriptionStatus.PENDING_PAYMENT) {
                    LocalDate start = invoice.getPeriodStart() != null ? invoice.getPeriodStart() : LocalDate.now();
                    LocalDate end = invoice.getPeriodEnd() != null ? invoice.getPeriodEnd() : start.plusMonths(1);
                    subscription.activate(start, end);
                    subscriptionRepository.save(subscription);
                } else if (subscription.getStatus() == SubscriptionStatus.SUSPENDED) {
                    subscription.restore();
                    subscriptionRepository.save(subscription);
                }
            }
        }

        return new MarkInvoicePaidResult(invoice.getId(), invoice.getStatus().name());
    }
}
