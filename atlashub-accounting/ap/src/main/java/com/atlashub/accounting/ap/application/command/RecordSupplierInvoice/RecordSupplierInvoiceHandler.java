package com.atlashub.accounting.ap.application.command.RecordSupplierInvoice;

import com.atlashub.accounting.ap.domain.entities.SupplierInvoice;
import com.atlashub.accounting.ap.domain.repositories.SupplierInvoiceRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RecordSupplierInvoiceHandler extends Command<RecordSupplierInvoiceCommand, Long> {

    private final SupplierInvoiceRepository invoiceRepository;

    public RecordSupplierInvoiceHandler(SupplierInvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public Long execute(RecordSupplierInvoiceCommand command) {
        Optional<SupplierInvoice> existing = invoiceRepository.findByOrganizationIdAndPurchaseOrderId(
                command.organizationId(),
                command.purchaseOrderId()
        );
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        Long invoiceId = invoiceRepository.nextIdentity();
        SupplierInvoice invoice = SupplierInvoice.create(
                invoiceId,
                command.organizationId(),
                command.supplierId(),
                command.purchaseOrderId(),
                command.invoiceNumber(),
                command.amount(),
                command.dueDate()
        );

        SupplierInvoice saved = invoiceRepository.save(invoice);
        return saved.getId();
    }
}
