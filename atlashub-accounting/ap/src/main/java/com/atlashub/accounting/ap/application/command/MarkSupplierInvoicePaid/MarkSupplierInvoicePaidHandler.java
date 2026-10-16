package com.atlashub.accounting.ap.application.command.MarkSupplierInvoicePaid;

import com.atlashub.accounting.ap.domain.entities.SupplierInvoice;
import com.atlashub.accounting.ap.domain.exceptions.SupplierInvoiceNotFoundException;
import com.atlashub.accounting.ap.domain.repositories.SupplierInvoiceRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class MarkSupplierInvoicePaidHandler extends Command<MarkSupplierInvoicePaidCommand, Void> {

    private final SupplierInvoiceRepository invoiceRepository;

    public MarkSupplierInvoicePaidHandler(SupplierInvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public Void execute(MarkSupplierInvoicePaidCommand command) {
        SupplierInvoice invoice = invoiceRepository.findById(command.invoiceId())
                .orElseThrow(() -> new SupplierInvoiceNotFoundException(command.invoiceId()));

        invoice.markPaid(command.payoutReference(), command.paidAt());
        invoiceRepository.save(invoice);
        return null;
    }
}
