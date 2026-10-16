package com.atlashub.billing.application.command.GenerateSubscriptionInvoice;

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
public class GenerateSubscriptionInvoiceHandler extends Command<GenerateSubscriptionInvoiceCommand, GenerateSubscriptionInvoiceResult> {

    private final SubscriptionRepository subscriptionRepository;
    private final BillingInvoiceRepository billingInvoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;

    public GenerateSubscriptionInvoiceHandler(
            SubscriptionRepository subscriptionRepository,
            BillingInvoiceRepository billingInvoiceRepository,
            InvoiceLineItemRepository invoiceLineItemRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.billingInvoiceRepository = billingInvoiceRepository;
        this.invoiceLineItemRepository = invoiceLineItemRepository;
    }

    @Override
    public GenerateSubscriptionInvoiceResult execute(GenerateSubscriptionInvoiceCommand command) {
        Subscription subscription = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found: " + command.subscriptionId()));

        Long invoiceId = billingInvoiceRepository.nextIdentity();
        String invoiceNumber = "INV-" + LocalDate.now().getYear() + "-" + String.format("%05d", invoiceId);
        BigDecimal subtotal = command.amount() != null ? command.amount() : BigDecimal.valueOf(50000);
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.075));

        BillingInvoice invoice = BillingInvoice.create(
                invoiceId,
                subscription.getOrganizationId(),
                subscription.getId(),
                invoiceNumber,
                "UNIVERSAL_SUBSCRIPTION",
                command.periodStart() != null ? command.periodStart() : LocalDate.now(),
                command.periodEnd() != null ? command.periodEnd() : LocalDate.now().plusMonths(1),
                subtotal,
                tax,
                subscription.getBillingCurrency(),
                LocalDate.now().plusDays(7)
        );

        Long lineItemId = invoiceLineItemRepository.nextIdentity();
        InvoiceLineItem lineItem = InvoiceLineItem.create(
                lineItemId,
                invoiceId,
                "AtlasHub Universal Subscription",
                1,
                subtotal,
                LineItemType.SUBSCRIPTION
        );
        invoiceLineItemRepository.save(lineItem);

        invoice.issue();
        billingInvoiceRepository.save(invoice);

        return new GenerateSubscriptionInvoiceResult(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getTotalAmount(),
                invoice.getStatus().name()
        );
    }
}
