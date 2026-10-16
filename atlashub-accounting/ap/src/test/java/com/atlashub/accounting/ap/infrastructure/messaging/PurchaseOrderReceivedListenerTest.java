package com.atlashub.accounting.ap.infrastructure.messaging;

import com.atlashub.accounting.ap.application.command.RecordSupplierInvoice.RecordSupplierInvoiceCommand;
import com.atlashub.accounting.ap.application.command.RecordSupplierInvoice.RecordSupplierInvoiceHandler;
import com.atlashub.accounting.ap.infrastructure.messaging.listeners.PurchaseOrderReceivedListener;
import com.atlashub.shared.application.port.EventTrackerPort;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PurchaseOrderReceivedListenerTest {

    private ObjectMapper objectMapper;
    private RecordSupplierInvoiceHandler handler;
    private EventTrackerPort eventTrackerPort;
    private PurchaseOrderReceivedListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        handler = mock(RecordSupplierInvoiceHandler.class);
        eventTrackerPort = mock(EventTrackerPort.class);
        listener = new PurchaseOrderReceivedListener(objectMapper, handler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTrackerPort);
        listener.init();
    }

    @Test
    @DisplayName("Should deserialize PurchaseOrderReceivedEvent and delegate to RecordSupplierInvoiceHandler")
    void shouldDelegatePurchaseOrderReceivedEventToHandler() {
        when(handler.execute(any())).thenReturn(1001L);

        String json = """
            {
                "eventType": "PurchaseOrderReceivedEvent",
                "event": {
                    "eventId": "evt-123",
                    "aggregateId": 300,
                    "occurredAt": "2026-10-14T10:00:00Z",
                    "correlationId": "corr-456",
                    "payload": {
                        "organizationId": 1,
                        "supplierId": 200,
                        "purchaseOrderId": 300,
                        "invoiceNumber": "INV-PO-300",
                        "amount": 250000.00,
                        "currency": "NGN",
                        "dueDate": "2026-11-15"
                    }
                }
            }
            """;

        listener.onPurchaseOrderReceived(json);

        ArgumentCaptor<RecordSupplierInvoiceCommand> captor = ArgumentCaptor.forClass(RecordSupplierInvoiceCommand.class);
        verify(handler).execute(captor.capture());

        RecordSupplierInvoiceCommand command = captor.getValue();
        assertNotNull(command);
        assertEquals(1L, command.organizationId());
        assertEquals(200L, command.supplierId());
        assertEquals(300L, command.purchaseOrderId());
        assertEquals("INV-PO-300", command.invoiceNumber());
        assertEquals(Money.of(new BigDecimal("250000.00"), CurrencyCode.NGN), command.amount());
        assertEquals(LocalDate.parse("2026-11-15"), command.dueDate());
    }

    @Test
    @DisplayName("Should ignore event if amount is zero or negative")
    void shouldIgnoreEventWithZeroAmount() {
        String json = """
            {
                "eventType": "PurchaseOrderReceivedEvent",
                "event": {
                    "eventId": "evt-124",
                    "aggregateId": 301,
                    "occurredAt": "2026-10-14T10:00:00Z",
                    "correlationId": "corr-457",
                    "payload": {
                        "organizationId": 1,
                        "supplierId": 200,
                        "purchaseOrderId": 301,
                        "invoiceNumber": "INV-PO-301",
                        "amount": 0.00,
                        "currency": "NGN",
                        "dueDate": "2026-11-15"
                    }
                }
            }
            """;

        listener.onPurchaseOrderReceived(json);

        verify(handler, never()).execute(any());
    }

    @Test
    @DisplayName("Should ignore unrelated event message")
    void shouldIgnoreUnrelatedMessage() {
        String json = """
            {
                "eventType": "SomeOtherEvent",
                "event": {
                    "eventId": "evt-999",
                    "aggregateId": 1,
                    "occurredAt": "2026-10-14T10:00:00Z",
                    "correlationId": "corr-999",
                    "payload": {}
                }
            }
            """;

        listener.onPurchaseOrderReceived(json);

        verify(handler, never()).execute(any());
    }
}
