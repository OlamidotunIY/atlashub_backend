package com.atlashub.commerce.storefront.infrastructure.messaging.listeners;

import com.atlashub.commerce.storefront.application.commands.FailPayment.FailPaymentCommand;
import com.atlashub.commerce.storefront.application.commands.FailPayment.FailPaymentHandler;
import com.atlashub.shared.application.port.EventTrackerPort;
import com.atlashub.shared.infrastructure.persistence.repository.DeadLetterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockReservationFailedListenerTest {

    private ObjectMapper objectMapper;

    @Mock
    private FailPaymentHandler handler;

    @Mock
    private EventTrackerPort eventTrackerPort;

    @Mock
    private DeadLetterRepository deadLetterRepository;

    private StockReservationFailedListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listener = new StockReservationFailedListener(objectMapper, handler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTrackerPort);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", deadLetterRepository);
        doAnswer(invocation -> null).when(handler).execute(any(FailPaymentCommand.class));
        listener.init();
    }

    @Test
    @DisplayName("Should process StockReservationFailedEvent and delegate to FailPaymentHandler")
    void shouldProcessEventAndCallHandler() {
        String jsonPayload = """
                {
                    "eventId": "evt-srf-1",
                    "eventType": "StockReservationFailedEvent",
                    "occurredAt": "2026-10-10T12:00:00Z",
                    "payload": {
                        "salesOrderId": 888,
                        "organizationId": 10,
                        "outletId": 20,
                        "failureReason": "Out of stock"
                    }
                }
                """;

        listener.listen(jsonPayload);

        ArgumentCaptor<FailPaymentCommand> captor = ArgumentCaptor.forClass(FailPaymentCommand.class);
        verify(handler).execute(captor.capture());
        assertNotNull(captor.getValue());
        assertEquals(888L, captor.getValue().salesOrderId());
        assertEquals("Out of stock", captor.getValue().reason());
    }
}
