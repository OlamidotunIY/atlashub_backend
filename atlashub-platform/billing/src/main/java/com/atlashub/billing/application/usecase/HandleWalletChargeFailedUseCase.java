package com.atlashub.billing.application.usecase;

import com.atlashub.billing.application.command.HandleWalletChargeFailedCommand;

import com.atlashub.billing.domain.events.ExternalChargeRequestedEvent;
import com.atlashub.billing.domain.model.BillingInvoice;
import com.atlashub.billing.domain.model.OrganizationProduct;
import com.atlashub.billing.domain.repository.BillingInvoiceRepository;
import com.atlashub.billing.domain.repository.OrganizationProductRepository;
import com.atlashub.shared.application.usecase.BaseUseCase;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.domain.exception.SharedErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;



@Service
public class HandleWalletChargeFailedUseCase extends BaseUseCase<HandleWalletChargeFailedCommand, Void> {

    private final BillingInvoiceRepository invoiceRepository;
    private final OrganizationProductRepository orgProductRepository;
    private final DomainEventPublisher publisher;

    public HandleWalletChargeFailedUseCase(
            BillingInvoiceRepository invoiceRepository, 
            OrganizationProductRepository orgProductRepository, 
            DomainEventPublisher publisher) {
        this.invoiceRepository = invoiceRepository;
        this.orgProductRepository = orgProductRepository;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public Void execute(HandleWalletChargeFailedCommand command) {
        BillingInvoice invoice = invoiceRepository.findById(command.invoiceId())
                .orElseThrow(() -> new NotFoundException(SharedErrorCode.INVALID_ID, "Invoice not found"));

        OrganizationProduct orgProduct = orgProductRepository.findById(invoice.getOrganizationProductId())
                .orElseThrow(() -> new NotFoundException(SharedErrorCode.INVALID_ID, "Org product not found"));

        ExternalChargeRequestedEvent event = new ExternalChargeRequestedEvent(
                UUID.randomUUID().toString(),
                String.valueOf(invoice.getId()),
                ZonedDateTime.now(),
                new ExternalChargeRequestedEvent.Payload(
                        invoice.getId(),
                        orgProduct.getOrganizationId(),
                        invoice.getAmount().amount(),
                        invoice.getAmount().currency().name(),
                        "admin@organization.com", // In a real app, query org admin email
                        "https://frontend.atlashub.com/checkout/success"
                )
        );
        publisher.publish(EnvelopedDomainEvent.wrap(event));
        
        return null;
    }
}
