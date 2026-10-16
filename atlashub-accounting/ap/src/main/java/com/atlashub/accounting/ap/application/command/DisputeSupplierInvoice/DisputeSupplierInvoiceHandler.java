package com.atlashub.accounting.ap.application.command.DisputeSupplierInvoice;

import com.atlashub.accounting.ap.domain.entities.SupplierInvoice;
import com.atlashub.accounting.ap.domain.exceptions.SupplierInvoiceNotFoundException;
import com.atlashub.accounting.ap.domain.repositories.SupplierInvoiceRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class DisputeSupplierInvoiceHandler extends Command<DisputeSupplierInvoiceCommand, Void> {

    private final SupplierInvoiceRepository invoiceRepository;

    public DisputeSupplierInvoiceHandler(SupplierInvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public Void execute(DisputeSupplierInvoiceCommand command) {
        SupplierInvoice invoice = invoiceRepository.findById(command.invoiceId())
                .orElseThrow(() -> new SupplierInvoiceNotFoundException(command.invoiceId()));

        invoice.dispute(command.reason());
        invoiceRepository.save(invoice);
        return null;
    }
}
