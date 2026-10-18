package com.atlashub.billing.application.command.InitialiseSubscription;

import com.atlashub.billing.domain.entities.BillingInvoice;
import com.atlashub.billing.domain.entities.InvoiceLineItem;
import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.repositories.BillingInvoiceRepository;
import com.atlashub.billing.domain.repositories.InvoiceLineItemRepository;
import com.atlashub.billing.domain.repositories.SubscriptionRepository;
import com.atlashub.billing.domain.valueobject.LineItemType;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Component
public class InitialiseSubscriptionHandler extends Command<InitialiseSubscriptionCommand, InitialiseSubscriptionResult> {

    private final SubscriptionRepository subscriptionRepository;
    private final BillingInvoiceRepository billingInvoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;

    public InitialiseSubscriptionHandler(
            SubscriptionRepository subscriptionRepository,
            BillingInvoiceRepository billingInvoiceRepository,
            InvoiceLineItemRepository invoiceLineItemRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.billingInvoiceRepository = billingInvoiceRepository;
        this.invoiceLineItemRepository = invoiceLineItemRepository;
    }

    @Override
    public InitialiseSubscriptionResult execute(InitialiseSubscriptionCommand command) {
        Optional<Subscription> existing = subscriptionRepository.findByOrganizationId(command.organizationId());
        if (existing.isPresent()) {
            Subscription sub = existing.get();
            return new InitialiseSubscriptionResult(sub.getId(), sub.getOrganizationId(), sub.getStatus().name());
        }

        Long subscriptionId = subscriptionRepository.nextIdentity();
        String currency = (command.currency() != null && !command.currency().isBlank()) ? command.currency() : "NGN";
        Long planId = command.pricingPlanId() != null ? command.pricingPlanId() : 1L;
        LocalDate trialEndsAt = LocalDate.now().plusDays(30);

        Subscription subscription = Subscription.create(
                subscriptionId,
                command.organizationId(),
                planId,
                currency,
                trialEndsAt
        );
        subscriptionRepository.save(subscription);

        // Immediately generate initial subscription invoice for first period
        Long invoiceId = billingInvoiceRepository.nextIdentity();
        String invoiceNumber = "INV-" + LocalDate.now().getYear() + "-" + String.format("%05d", invoiceId);
        LocalDate periodStart = LocalDate.now();
        LocalDate periodEnd = LocalDate.now().plusDays(30);
        BigDecimal planAmount = BigDecimal.valueOf(50000); // Base starter subscription fee
        BigDecimal tax = planAmount.multiply(BigDecimal.valueOf(0.075)); // 7.5% VAT

        BillingInvoice invoice = BillingInvoice.create(
                invoiceId,
                command.organizationId(),
                subscriptionId,
                invoiceNumber,
                "UNIVERSAL_SUBSCRIPTION",
                periodStart,
                periodEnd,
                planAmount,
                tax,
                currency,
                LocalDate.now().plusDays(7)
        );

        Long lineItemId = invoiceLineItemRepository.nextIdentity();
        InvoiceLineItem lineItem = InvoiceLineItem.create(
                lineItemId,
                invoiceId,
                "AtlasHub Universal Subscription (30 Days)",
                1,
                planAmount,
                LineItemType.SUBSCRIPTION
        );
        invoiceLineItemRepository.save(lineItem);

        invoice.issue();
        billingInvoiceRepository.save(invoice);

        return new InitialiseSubscriptionResult(
                subscription.getId(),
                subscription.getOrganizationId(),
                subscription.getStatus().name()
        );
    }
}
