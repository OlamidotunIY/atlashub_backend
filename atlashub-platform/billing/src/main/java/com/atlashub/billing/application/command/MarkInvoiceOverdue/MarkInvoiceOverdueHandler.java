package com.atlashub.billing.application.command.MarkInvoiceOverdue;

import com.atlashub.billing.domain.entities.BillingInvoice;
import com.atlashub.billing.domain.exceptions.InvoiceNotFoundException;
import com.atlashub.billing.domain.repositories.BillingInvoiceRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class MarkInvoiceOverdueHandler extends Command<MarkInvoiceOverdueCommand, MarkInvoiceOverdueResult> {

    private final BillingInvoiceRepository billingInvoiceRepository;

    public MarkInvoiceOverdueHandler(BillingInvoiceRepository billingInvoiceRepository) {
        this.billingInvoiceRepository = billingInvoiceRepository;
    }

    @Override
    public MarkInvoiceOverdueResult execute(MarkInvoiceOverdueCommand command) {
        BillingInvoice invoice = billingInvoiceRepository.findById(command.invoiceId())
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found: " + command.invoiceId()));

        invoice.markOverdue();
        billingInvoiceRepository.save(invoice);

        return new MarkInvoiceOverdueResult(invoice.getId(), invoice.getStatus().name());
    }
}
