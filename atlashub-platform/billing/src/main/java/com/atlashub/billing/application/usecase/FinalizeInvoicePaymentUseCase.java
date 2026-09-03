package com.atlashub.billing.application.usecase;

import com.atlashub.billing.application.command.FinalizeInvoicePaymentCommand;

import com.atlashub.billing.domain.model.BillingInvoice;
import com.atlashub.billing.domain.repository.BillingInvoiceRepository;
import com.atlashub.shared.application.usecase.BaseUseCase;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.domain.exception.SharedErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;



@Service
public class FinalizeInvoicePaymentUseCase extends BaseUseCase<FinalizeInvoicePaymentCommand, Void> {

    private final BillingInvoiceRepository invoiceRepository;
    private final DomainEventPublisher publisher;

    public FinalizeInvoicePaymentUseCase(BillingInvoiceRepository invoiceRepository, DomainEventPublisher publisher) {
        this.invoiceRepository = invoiceRepository;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public Void execute(FinalizeInvoicePaymentCommand command) {
        BillingInvoice invoice = invoiceRepository.findById(command.invoiceId())
                .orElseThrow(() -> new NotFoundException(SharedErrorCode.INVALID_ID, "Invoice not found"));

        invoice.markAsPaid(ZonedDateTime.now());
        BillingInvoice saved = invoiceRepository.save(invoice);
        
        publishEvents(saved, publisher);
        return null;
    }
}
