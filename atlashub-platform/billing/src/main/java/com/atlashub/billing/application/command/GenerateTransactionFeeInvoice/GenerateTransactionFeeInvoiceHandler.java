package com.atlashub.billing.application.command.GenerateTransactionFeeInvoice;

import com.atlashub.billing.domain.entities.BillingInvoice;
import com.atlashub.billing.domain.entities.InvoiceLineItem;
import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.entities.TransactionVolumeLedger;
import com.atlashub.billing.domain.exceptions.BillingDomainException;
import com.atlashub.billing.domain.repositories.BillingInvoiceRepository;
import com.atlashub.billing.domain.repositories.InvoiceLineItemRepository;
import com.atlashub.billing.domain.repositories.SubscriptionRepository;
import com.atlashub.billing.domain.repositories.TransactionVolumeLedgerRepository;
import com.atlashub.billing.domain.valueobject.LineItemType;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Component
public class GenerateTransactionFeeInvoiceHandler extends Command<GenerateTransactionFeeInvoiceCommand, GenerateTransactionFeeInvoiceResult> {

    private final SubscriptionRepository subscriptionRepository;
    private final TransactionVolumeLedgerRepository ledgerRepository;
    private final BillingInvoiceRepository billingInvoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;

    public GenerateTransactionFeeInvoiceHandler(
            SubscriptionRepository subscriptionRepository,
            TransactionVolumeLedgerRepository ledgerRepository,
            BillingInvoiceRepository billingInvoiceRepository,
            InvoiceLineItemRepository invoiceLineItemRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.ledgerRepository = ledgerRepository;
        this.billingInvoiceRepository = billingInvoiceRepository;
        this.invoiceLineItemRepository = invoiceLineItemRepository;
    }

    @Override
    public GenerateTransactionFeeInvoiceResult execute(GenerateTransactionFeeInvoiceCommand command) {
        Subscription subscription = subscriptionRepository.findByOrganizationId(command.organizationId())
                .orElse(null);
        Long subscriptionId = subscription != null ? subscription.getId() : null;
        String currency = subscription != null ? subscription.getBillingCurrency() : "NGN";

        Optional<TransactionVolumeLedger> ledgerOpt = ledgerRepository.findByOrganizationIdAndMonth(
                command.organizationId(), command.month()
        );

        BigDecimal volume = ledgerOpt.map(TransactionVolumeLedger::getTotalVolume).orElse(BigDecimal.ZERO);
        long txCount = ledgerOpt.map(TransactionVolumeLedger::getTotalTransactionCount).orElse(0L);

        // Tiered calculation: 1.5% fee on transaction volume
        BigDecimal subtotal = volume.multiply(BigDecimal.valueOf(0.015));
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.075));

        Long invoiceId = billingInvoiceRepository.nextIdentity();
        String invoiceNumber = "INV-FEE-" + command.month() + "-" + String.format("%05d", invoiceId);
        LocalDate now = LocalDate.now();

        BillingInvoice invoice = BillingInvoice.create(
                invoiceId,
                command.organizationId(),
                subscriptionId,
                invoiceNumber,
                "ATLAS_PAY_FEES",
                now.withDayOfMonth(1),
                now,
                subtotal,
                tax,
                currency,
                now.plusDays(7)
        );

        Long lineItemId = invoiceLineItemRepository.nextIdentity();
        InvoiceLineItem lineItem = InvoiceLineItem.create(
                lineItemId,
                invoiceId,
                "Atlas Pay Processing Fees for " + command.month() + " (" + txCount + " txns)",
                1,
                subtotal,
                LineItemType.TRANSACTION_FEE
        );
        invoiceLineItemRepository.save(lineItem);

        invoice.issue();
        billingInvoiceRepository.save(invoice);

        return new GenerateTransactionFeeInvoiceResult(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getTotalAmount(),
                invoice.getStatus().name()
        );
    }
}
