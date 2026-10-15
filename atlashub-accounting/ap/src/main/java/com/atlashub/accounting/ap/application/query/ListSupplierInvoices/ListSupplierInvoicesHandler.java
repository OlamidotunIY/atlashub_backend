package com.atlashub.accounting.ap.application.query.ListSupplierInvoices;

import com.atlashub.accounting.ap.domain.entities.SupplierInvoice;
import com.atlashub.accounting.ap.domain.repositories.SupplierInvoiceRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListSupplierInvoicesHandler extends Query<ListSupplierInvoicesQuery, List<SupplierInvoiceResult>> {

    private final SupplierInvoiceRepository invoiceRepository;

    public ListSupplierInvoicesHandler(SupplierInvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public List<SupplierInvoiceResult> execute(ListSupplierInvoicesQuery query) {
        List<SupplierInvoice> invoices;
        if (query.status() != null) {
            invoices = invoiceRepository.findByOrganizationIdAndStatus(query.organizationId(), query.status());
        } else if (query.supplierId() != null) {
            invoices = invoiceRepository.findByOrganizationIdAndSupplierId(query.organizationId(), query.supplierId());
        } else {
            invoices = invoiceRepository.findByOrganizationId(query.organizationId());
        }

        return invoices.stream()
                .map(this::toResult)
                .toList();
    }

    private SupplierInvoiceResult toResult(SupplierInvoice invoice) {
        return new SupplierInvoiceResult(
                invoice.getId(),
                invoice.getOrganizationId(),
                invoice.getSupplierId(),
                invoice.getPurchaseOrderId(),
                invoice.getInvoiceNumber(),
                invoice.getAmount(),
                invoice.getDueDate(),
                invoice.getStatus(),
                invoice.getPaidAt(),
                invoice.getCreatedAt()
        );
    }
}
