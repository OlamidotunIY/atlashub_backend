package com.atlashub.billing.application.command.RenewSubscription;

import com.atlashub.billing.domain.entities.BillingInvoice;
import com.atlashub.billing.domain.entities.InvoiceLineItem;
import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.exceptions.SubscriptionNotFoundException;
import com.atlashub.billing.domain.repositories.BillingInvoiceRepository;
import com.atlashub.billing.domain.repositories.InvoiceLineItemRepository;
import com.atlashub.billing.domain.repositories.SubscriptionRepository;
import com.atlashub.billing.domain.valueobject.LineItemType;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class RenewSubscriptionHandler extends Command<RenewSubscriptionCommand, RenewSubscriptionResult> {

    private final SubscriptionRepository subscriptionRepository;
    private final BillingInvoiceRepository billingInvoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;

    public RenewSubscriptionHandler(
            SubscriptionRepository subscriptionRepository,
            BillingInvoiceRepository billingInvoiceRepository,
            InvoiceLineItemRepository invoiceLineItemRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.billingInvoiceRepository = billingInvoiceRepository;
        this.invoiceLineItemRepository = invoiceLineItemRepository;
    }

    @Override
    public RenewSubscriptionResult execute(RenewSubscriptionCommand command) {
        Subscription subscription = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found: " + command.subscriptionId()));

        LocalDate start = subscription.getCurrentPeriodEnd() != null ? subscription.getCurrentPeriodEnd() : LocalDate.now();
        LocalDate end = start.plusMonths(1);

        subscription.renew(start, end);
        subscriptionRepository.save(subscription);

        // Generate renewal invoice
        Long invoiceId = billingInvoiceRepository.nextIdentity();
        String invoiceNumber = "INV-" + LocalDate.now().getYear() + "-" + String.format("%05d", invoiceId);
        BigDecimal planAmount = BigDecimal.valueOf(50000);
        BigDecimal tax = planAmount.multiply(BigDecimal.valueOf(0.075));

        BillingInvoice invoice = BillingInvoice.create(
                invoiceId,
                subscription.getOrganizationId(),
                subscription.getId(),
                invoiceNumber,
                "UNIVERSAL_SUBSCRIPTION",
                start,
                end,
                planAmount,
                tax,
                subscription.getBillingCurrency(),
                LocalDate.now().plusDays(7)
        );

        Long lineItemId = invoiceLineItemRepository.nextIdentity();
        InvoiceLineItem lineItem = InvoiceLineItem.create(
                lineItemId,
                invoiceId,
                "AtlasHub Universal Subscription Renewal",
                1,
                planAmount,
                LineItemType.SUBSCRIPTION
        );
        invoiceLineItemRepository.save(lineItem);

        invoice.issue();
        billingInvoiceRepository.save(invoice);

        return new RenewSubscriptionResult(
                subscription.getId(),
                subscription.getStatus().name(),
                start,
                end
        );
    }
}
