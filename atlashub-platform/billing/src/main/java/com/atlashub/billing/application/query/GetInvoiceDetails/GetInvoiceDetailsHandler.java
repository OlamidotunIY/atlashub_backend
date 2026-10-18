package com.atlashub.billing.application.query.GetInvoiceDetails;

import com.atlashub.billing.domain.entities.BillingInvoice;
import com.atlashub.billing.domain.entities.InvoiceLineItem;
import com.atlashub.billing.domain.exceptions.InvoiceNotFoundException;
import com.atlashub.billing.domain.repositories.BillingInvoiceRepository;
import com.atlashub.billing.domain.repositories.InvoiceLineItemRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetInvoiceDetailsHandler extends Query<GetInvoiceDetailsQuery, InvoiceDetailsResult> {

    private final BillingInvoiceRepository billingInvoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;

    public GetInvoiceDetailsHandler(
            BillingInvoiceRepository billingInvoiceRepository,
            InvoiceLineItemRepository invoiceLineItemRepository
    ) {
        this.billingInvoiceRepository = billingInvoiceRepository;
        this.invoiceLineItemRepository = invoiceLineItemRepository;
    }

    @Override
    public InvoiceDetailsResult execute(GetInvoiceDetailsQuery query) {
        BillingInvoice invoice = billingInvoiceRepository.findById(query.invoiceId())
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found: " + query.invoiceId()));

        List<InvoiceLineItem> lineItems = invoiceLineItemRepository.findByInvoiceId(invoice.getId());

        List<InvoiceLineItemResult> lineItemResults = lineItems.stream()
                .map(item -> new InvoiceLineItemResult(
                        item.getId(),
                        item.getInvoiceId(),
                        item.getDescription(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalAmount(),
                        item.getLineItemType().name()
                ))
                .toList();

        return new InvoiceDetailsResult(
                invoice.getId(),
                invoice.getOrganizationId(),
                invoice.getSubscriptionId(),
                invoice.getInvoiceNumber(),
                invoice.getProductCode(),
                invoice.getPeriodStart(),
                invoice.getPeriodEnd(),
                invoice.getSubtotal(),
                invoice.getTax(),
                invoice.getTotalAmount(),
                invoice.getCurrency(),
                invoice.getStatus().name(),
                invoice.getDueDate(),
                invoice.getIssuedAt(),
                invoice.getPaidAt(),
                invoice.getPaymentReference(),
                invoice.getPaymentCheckoutUrl(),
                lineItemResults
        );
    }
}
