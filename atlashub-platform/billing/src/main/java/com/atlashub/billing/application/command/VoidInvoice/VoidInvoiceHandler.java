package com.atlashub.billing.application.command.VoidInvoice;

import com.atlashub.billing.domain.entities.BillingInvoice;
import com.atlashub.billing.domain.exceptions.InvoiceNotFoundException;
import com.atlashub.billing.domain.repositories.BillingInvoiceRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class VoidInvoiceHandler extends Command<VoidInvoiceCommand, VoidInvoiceResult> {

    private final BillingInvoiceRepository billingInvoiceRepository;

    public VoidInvoiceHandler(BillingInvoiceRepository billingInvoiceRepository) {
        this.billingInvoiceRepository = billingInvoiceRepository;
    }

    @Override
    public VoidInvoiceResult execute(VoidInvoiceCommand command) {
        BillingInvoice invoice = billingInvoiceRepository.findById(command.invoiceId())
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found: " + command.invoiceId()));

        invoice.voidInvoice(command.reason() != null ? command.reason() : "Voided by admin");
        billingInvoiceRepository.save(invoice);

        return new VoidInvoiceResult(invoice.getId(), invoice.getStatus().name());
    }
}
