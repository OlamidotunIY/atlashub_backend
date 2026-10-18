package com.atlashub.billing.application.query.ListInvoices;

import com.atlashub.billing.domain.entities.BillingInvoice;
import com.atlashub.billing.domain.repositories.BillingInvoiceRepository;
import com.atlashub.billing.domain.valueobject.InvoiceStatus;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListInvoicesHandler extends Query<ListInvoicesQuery, List<InvoiceSummaryResult>> {

    private final BillingInvoiceRepository billingInvoiceRepository;

    public ListInvoicesHandler(BillingInvoiceRepository billingInvoiceRepository) {
        this.billingInvoiceRepository = billingInvoiceRepository;
    }

    @Override
    public List<InvoiceSummaryResult> execute(ListInvoicesQuery query) {
        List<BillingInvoice> invoices;
        if (query.status() != null && !query.status().isBlank()) {
            InvoiceStatus status = InvoiceStatus.valueOf(query.status().trim().toUpperCase());
            invoices = billingInvoiceRepository.findByOrganizationIdAndStatus(query.organizationId(), status);
        } else {
            invoices = billingInvoiceRepository.findByOrganizationId(query.organizationId());
        }

        return invoices.stream()
                .map(inv -> new InvoiceSummaryResult(
                        inv.getId(),
                        inv.getOrganizationId(),
                        inv.getSubscriptionId(),
                        inv.getInvoiceNumber(),
                        inv.getProductCode(),
                        inv.getPeriodStart(),
                        inv.getPeriodEnd(),
                        inv.getSubtotal(),
                        inv.getTax(),
                        inv.getTotalAmount(),
                        inv.getCurrency(),
                        inv.getStatus().name(),
                        inv.getDueDate(),
                        inv.getIssuedAt(),
                        inv.getPaidAt()
                ))
                .toList();
    }
}
