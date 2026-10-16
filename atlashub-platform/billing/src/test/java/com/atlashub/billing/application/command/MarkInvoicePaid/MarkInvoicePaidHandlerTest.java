package com.atlashub.billing.application.command.MarkInvoicePaid;

import com.atlashub.billing.domain.entities.BillingInvoice;
import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.repositories.BillingInvoiceRepository;
import com.atlashub.billing.domain.repositories.SubscriptionRepository;
import com.atlashub.billing.domain.valueobject.InvoiceStatus;
import com.atlashub.billing.domain.valueobject.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MarkInvoicePaidHandlerTest {

    private BillingInvoiceRepository billingInvoiceRepository;
    private SubscriptionRepository subscriptionRepository;
    private MarkInvoicePaidHandler handler;

    @BeforeEach
    void setUp() {
        billingInvoiceRepository = Mockito.mock(BillingInvoiceRepository.class);
        subscriptionRepository = Mockito.mock(SubscriptionRepository.class);
        handler = new MarkInvoicePaidHandler(billingInvoiceRepository, subscriptionRepository);
    }

    @Test
    void execute_marksInvoicePaid_andActivatesPendingSubscription() {
        BillingInvoice invoice = BillingInvoice.create(
                200L, 50L, 100L, "INV-2026-00001", "UNIVERSAL_SUBSCRIPTION",
                LocalDate.now(), LocalDate.now().plusMonths(1),
                BigDecimal.valueOf(50000), BigDecimal.valueOf(3750), "NGN", LocalDate.now().plusDays(7)
        );
        invoice.issue();

        Subscription subscription = Subscription.create(
                100L, 50L, 1L, "NGN", LocalDate.now().plusDays(30)
        );

        when(billingInvoiceRepository.findById(200L)).thenReturn(Optional.of(invoice));
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(subscription));

        MarkInvoicePaidCommand command = new MarkInvoicePaidCommand(200L, "PAYSTACK_REF_999", ZonedDateTime.now());
        MarkInvoicePaidResult result = handler.execute(command);

        assertEquals(200L, result.invoiceId());
        assertEquals(InvoiceStatus.PAID.name(), result.status());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        verify(billingInvoiceRepository).save(invoice);
        verify(subscriptionRepository).save(subscription);
    }
}
