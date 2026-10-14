package com.atlashub.accounting.cash.infrastructure.messaging.listeners;

import com.atlashub.accounting.cash.application.command.RecordCashEvacuation.RecordCashEvacuationCommand;
import com.atlashub.accounting.cash.application.command.RecordCashEvacuation.RecordCashEvacuationHandler;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TillClosedListenerTest {

    private ObjectMapper objectMapper;
    private RecordCashEvacuationHandler handler;
    private EventTrackerPort eventTrackerPort;
    private TillClosedListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        handler = mock(RecordCashEvacuationHandler.class);
        eventTrackerPort = mock(EventTrackerPort.class);
        listener = new TillClosedListener(objectMapper, handler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTrackerPort);
        listener.init();
    }

    @Test
    @DisplayName("Should deserialize TillClosedEvent and delegate to RecordCashEvacuationHandler")
    void shouldDelegateTillClosedEventToHandler() {
        when(handler.execute(any())).thenReturn(1001L);

        String json = """
            {
                "eventType": "TillClosedEvent",
                "event": {
                    "eventId": "evt-123",
                    "aggregateId": 50,
                    "occurredAt": "2026-10-14T10:00:00Z",
                    "correlationId": "corr-456",
                    "payload": {
                        "tillSessionId": 50,
                        "organizationId": 10,
                        "outletId": 20,
                        "cashierId": 30,
                        "closingCash": 150000.00,
                        "totalSales": 300000.00,
                        "currency": "NGN",
                        "closedAt": "2026-10-14T10:00:00Z"
                    }
                }
            }
            """;

        listener.onTillClosed(json);

        ArgumentCaptor<RecordCashEvacuationCommand> captor = ArgumentCaptor.forClass(RecordCashEvacuationCommand.class);
        verify(handler).execute(captor.capture());

        RecordCashEvacuationCommand command = captor.getValue();
        assertNotNull(command);
        assertEquals(10L, command.organizationId());
        assertEquals(20L, command.outletId());
        assertEquals(50L, command.tillId());
        assertEquals(Money.of(new BigDecimal("150000.00"), CurrencyCode.NGN), command.amount());
        assertEquals(30L, command.evacuatedBy());
    }

    @Test
    @DisplayName("Should ignore event if closing cash is zero or negative")
    void shouldIgnoreEventWithZeroClosingCash() {
        String json = """
            {
                "eventType": "TillClosedEvent",
                "event": {
                    "eventId": "evt-124",
                    "aggregateId": 51,
                    "occurredAt": "2026-10-14T10:00:00Z",
                    "correlationId": "corr-457",
                    "payload": {
                        "tillSessionId": 51,
                        "organizationId": 10,
                        "outletId": 20,
                        "cashierId": 30,
                        "closingCash": 0.00,
                        "totalSales": 0.00,
                        "currency": "NGN",
                        "closedAt": "2026-10-14T10:00:00Z"
                    }
                }
            }
            """;

        listener.onTillClosed(json);

        verify(handler, never()).execute(any());
    }

    @Test
    @DisplayName("Should ignore unrelated event message")
    void shouldIgnoreUnrelatedMessage() {
        String json = """
            {
                "eventId": "evt-999",
                "aggregateId": 1,
                "occurredAt": "2026-10-14T10:00:00Z",
                "correlationId": "corr-999",
                "payload": {}
            }
            """;

        listener.onTillClosed(json);

        verify(handler, never()).execute(any());
    }
}
