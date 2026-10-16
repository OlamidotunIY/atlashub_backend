package com.atlashub.accounting.ap.infrastructure.messaging;

import com.atlashub.accounting.ap.application.command.MarkSupplierInvoicePaid.MarkSupplierInvoicePaidCommand;
import com.atlashub.accounting.ap.application.command.MarkSupplierInvoicePaid.MarkSupplierInvoicePaidHandler;
import com.atlashub.accounting.ap.infrastructure.messaging.listeners.PayoutCompletedListener;
import com.atlashub.shared.application.port.EventTrackerPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PayoutCompletedListenerTest {

    private ObjectMapper objectMapper;
    private MarkSupplierInvoicePaidHandler handler;
    private EventTrackerPort eventTrackerPort;
    private PayoutCompletedListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        handler = mock(MarkSupplierInvoicePaidHandler.class);
        eventTrackerPort = mock(EventTrackerPort.class);
        listener = new PayoutCompletedListener(objectMapper, handler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTrackerPort);
        listener.init();
    }

    @Test
    @DisplayName("Should deserialize PayoutCompletedEvent and delegate to MarkSupplierInvoicePaidHandler")
    void shouldDelegatePayoutCompletedEventToHandler() {
        String json = """
            {
                "eventType": "PayoutCompletedEvent",
                "event": {
                    "eventId": "evt-777",
                    "aggregateId": 500,
                    "occurredAt": "2026-10-14T12:00:00Z",
                    "correlationId": "corr-777",
                    "payload": {
                        "invoiceId": 500,
                        "payoutReference": "PAYOUT-REF-888",
                        "paidAt": "2026-10-14T12:00:00Z"
                    }
                }
            }
            """;

        listener.onPayoutCompleted(json);

        ArgumentCaptor<MarkSupplierInvoicePaidCommand> captor = ArgumentCaptor.forClass(MarkSupplierInvoicePaidCommand.class);
        verify(handler).execute(captor.capture());

        MarkSupplierInvoicePaidCommand command = captor.getValue();
        assertNotNull(command);
        assertEquals(500L, command.invoiceId());
        assertEquals("PAYOUT-REF-888", command.payoutReference());
        assertNotNull(command.paidAt());
    }

    @Test
    @DisplayName("Should ignore event if invoiceId is null")
    void shouldIgnoreEventWithNullInvoiceId() {
        String json = """
            {
                "eventType": "PayoutCompletedEvent",
                "event": {
                    "eventId": "evt-778",
                    "aggregateId": 0,
                    "occurredAt": "2026-10-14T12:00:00Z",
                    "correlationId": "corr-778",
                    "payload": {
                        "payoutReference": "PAYOUT-REF-888"
                    }
                }
            }
            """;

        listener.onPayoutCompleted(json);

        verify(handler, never()).execute(any());
    }

    @Test
    @DisplayName("Should ignore unrelated event message")
    void shouldIgnoreUnrelatedMessage() {
        String json = """
            {
                "eventType": "UnrelatedEvent",
                "event": {
                    "eventId": "evt-999",
                    "aggregateId": 1,
                    "occurredAt": "2026-10-14T10:00:00Z",
                    "correlationId": "corr-999",
                    "payload": {}
                }
            }
            """;

        listener.onPayoutCompleted(json);

        verify(handler, never()).execute(any());
    }
}
