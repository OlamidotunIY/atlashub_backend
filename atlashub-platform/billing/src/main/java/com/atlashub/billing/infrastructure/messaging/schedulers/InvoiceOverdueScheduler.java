package com.atlashub.billing.infrastructure.messaging.schedulers;

import com.atlashub.billing.application.command.MarkInvoiceOverdue.MarkInvoiceOverdueCommand;
import com.atlashub.billing.application.command.MarkInvoiceOverdue.MarkInvoiceOverdueHandler;
import com.atlashub.billing.domain.entities.BillingInvoice;
import com.atlashub.billing.domain.repositories.BillingInvoiceRepository;
import com.atlashub.billing.domain.valueobject.InvoiceStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class InvoiceOverdueScheduler {

    private final BillingInvoiceRepository billingInvoiceRepository;
    private final MarkInvoiceOverdueHandler markInvoiceOverdueHandler;

    public InvoiceOverdueScheduler(
            BillingInvoiceRepository billingInvoiceRepository,
            MarkInvoiceOverdueHandler markInvoiceOverdueHandler
    ) {
        this.billingInvoiceRepository = billingInvoiceRepository;
        this.markInvoiceOverdueHandler = markInvoiceOverdueHandler;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void markOverdueInvoices() {
        List<BillingInvoice> overdueInvoices = billingInvoiceRepository.findByStatusAndDueDateBefore(
                InvoiceStatus.ISSUED,
                LocalDate.now()
        );

        for (BillingInvoice invoice : overdueInvoices) {
            markInvoiceOverdueHandler.execute(new MarkInvoiceOverdueCommand(invoice.getId()));
        }
    }
}
