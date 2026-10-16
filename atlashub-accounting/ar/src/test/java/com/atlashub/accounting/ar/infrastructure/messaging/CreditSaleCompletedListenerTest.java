package com.atlashub.accounting.ar.infrastructure.messaging;

import com.atlashub.accounting.ar.application.command.CreateCustomerReceivable.CreateCustomerReceivableCommand;
import com.atlashub.accounting.ar.application.command.CreateCustomerReceivable.CreateCustomerReceivableHandler;
import com.atlashub.accounting.ar.infrastructure.messaging.listeners.CreditSaleCompletedListener;
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

class CreditSaleCompletedListenerTest {

    private ObjectMapper objectMapper;
    private CreateCustomerReceivableHandler handler;
    private EventTrackerPort eventTrackerPort;
    private CreditSaleCompletedListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        handler = mock(CreateCustomerReceivableHandler.class);
        eventTrackerPort = mock(EventTrackerPort.class);
        listener = new CreditSaleCompletedListener(objectMapper, handler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTrackerPort);
        listener.init();
    }

    @Test
    @DisplayName("Should deserialize CreditSaleCompletedEvent and delegate to CreateCustomerReceivableHandler")
    void shouldDelegateCreditSaleCompletedEventToHandler() {
        when(handler.execute(any())).thenReturn(1001L);

        String json = """
            {
                "eventType": "CreditSaleCompletedEvent",
                "event": {
                    "eventId": "evt-credit-123",
                    "aggregateId": 300,
                    "occurredAt": "2026-10-14T10:00:00Z",
                    "correlationId": "corr-credit-456",
                    "payload": {
                        "organizationId": 1,
                        "customerId": 200,
                        "salesOrderId": 300,
                        "amount": 180000.00,
                        "currency": "NGN",
                        "dueDate": "2026-11-20"
                    }
                }
            }
            """;

        listener.onCreditSaleCompleted(json);

        ArgumentCaptor<CreateCustomerReceivableCommand> captor = ArgumentCaptor.forClass(CreateCustomerReceivableCommand.class);
        verify(handler).execute(captor.capture());

        CreateCustomerReceivableCommand command = captor.getValue();
        assertNotNull(command);
        assertEquals(1L, command.organizationId());
        assertEquals(200L, command.customerId());
        assertEquals(300L, command.salesOrderId());
        assertEquals(Money.of(new BigDecimal("180000.00"), CurrencyCode.NGN), command.amount());
        assertEquals(LocalDate.parse("2026-11-20"), command.dueDate());
    }

    @Test
    @DisplayName("Should ignore event if amount is zero or negative")
    void shouldIgnoreEventWithZeroAmount() {
        String json = """
            {
                "eventType": "CreditSaleCompletedEvent",
                "event": {
                    "eventId": "evt-credit-124",
                    "aggregateId": 301,
                    "occurredAt": "2026-10-14T10:00:00Z",
                    "correlationId": "corr-credit-457",
                    "payload": {
                        "organizationId": 1,
                        "customerId": 200,
                        "salesOrderId": 301,
                        "amount": 0.00,
                        "currency": "NGN",
                        "dueDate": "2026-11-20"
                    }
                }
            }
            """;

        listener.onCreditSaleCompleted(json);

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

        listener.onCreditSaleCompleted(json);

        verify(handler, never()).execute(any());
    }
}
