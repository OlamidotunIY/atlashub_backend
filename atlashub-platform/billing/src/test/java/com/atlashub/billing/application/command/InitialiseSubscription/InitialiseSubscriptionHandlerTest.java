package com.atlashub.billing.application.command.InitialiseSubscription;

import com.atlashub.billing.domain.entities.BillingInvoice;
import com.atlashub.billing.domain.entities.InvoiceLineItem;
import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.repositories.BillingInvoiceRepository;
import com.atlashub.billing.domain.repositories.InvoiceLineItemRepository;
import com.atlashub.billing.domain.repositories.SubscriptionRepository;
import com.atlashub.billing.domain.valueobject.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InitialiseSubscriptionHandlerTest {

    private SubscriptionRepository subscriptionRepository;
    private BillingInvoiceRepository billingInvoiceRepository;
    private InvoiceLineItemRepository invoiceLineItemRepository;
    private InitialiseSubscriptionHandler handler;

    @BeforeEach
    void setUp() {
        subscriptionRepository = Mockito.mock(SubscriptionRepository.class);
        billingInvoiceRepository = Mockito.mock(BillingInvoiceRepository.class);
        invoiceLineItemRepository = Mockito.mock(InvoiceLineItemRepository.class);
        handler = new InitialiseSubscriptionHandler(
                subscriptionRepository,
                billingInvoiceRepository,
                invoiceLineItemRepository
        );
    }

    @Test
    void execute_createsSubscriptionAndInitialInvoice() {
        when(subscriptionRepository.findByOrganizationId(50L)).thenReturn(Optional.empty());
        when(subscriptionRepository.nextIdentity()).thenReturn(100L);
        when(billingInvoiceRepository.nextIdentity()).thenReturn(200L);
        when(invoiceLineItemRepository.nextIdentity()).thenReturn(300L);

        InitialiseSubscriptionCommand command = new InitialiseSubscriptionCommand(50L, 1L, "NGN");
        InitialiseSubscriptionResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(100L, result.subscriptionId());
        assertEquals(50L, result.organizationId());
        assertEquals(SubscriptionStatus.PENDING_PAYMENT.name(), result.status());

        verify(subscriptionRepository).save(any(Subscription.class));
        verify(billingInvoiceRepository).save(any(BillingInvoice.class));
        verify(invoiceLineItemRepository).save(any(InvoiceLineItem.class));
    }

    @Test
    void execute_whenAlreadyExists_returnsExisting() {
        Subscription existing = Subscription.create(100L, 50L, 1L, "NGN", LocalDate.now().plusDays(30));
        when(subscriptionRepository.findByOrganizationId(50L)).thenReturn(Optional.of(existing));

        InitialiseSubscriptionCommand command = new InitialiseSubscriptionCommand(50L, 1L, "NGN");
        InitialiseSubscriptionResult result = handler.execute(command);

        assertEquals(100L, result.subscriptionId());
        assertEquals(50L, result.organizationId());
        assertEquals(SubscriptionStatus.PENDING_PAYMENT.name(), result.status());
    }
}
