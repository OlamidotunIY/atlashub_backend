package com.atlashub.commerce.inventory.infrastructure.messaging.listeners;

import com.atlashub.commerce.inventory.application.commands.ReserveStockForOrder.ReserveStockForOrderCommand;
import com.atlashub.commerce.inventory.application.commands.ReserveStockForOrder.ReserveStockForOrderHandler;
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
class SalesOrderCreatedListenerTest {

    private ObjectMapper objectMapper;

    @Mock
    private ReserveStockForOrderHandler handler;

    @Mock
    private EventTrackerPort eventTrackerPort;

    @Mock
    private DeadLetterRepository deadLetterRepository;

    private SalesOrderCreatedListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listener = new SalesOrderCreatedListener(objectMapper, handler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTrackerPort);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", deadLetterRepository);
        doAnswer(invocation -> null).when(handler).execute(any(ReserveStockForOrderCommand.class));
        listener.init();
    }

    @Test
    @DisplayName("Should process SalesOrderCreatedEvent and delegate to ReserveStockForOrderHandler")
    void shouldProcessEventAndCallHandler() {
        String jsonPayload = """
                {
                    "eventId": "evt-order-1",
                    "eventType": "SalesOrderCreatedEvent",
                    "occurredAt": "2026-10-10T12:00:00Z",
                    "payload": {
                        "salesOrderId": 555,
                        "organizationId": 10,
                        "outletId": 20,
                        "items": [
                            { "productId": 100, "variantId": null, "quantity": 3 }
                        ]
                    }
                }
                """;

        listener.listen(jsonPayload);

        ArgumentCaptor<ReserveStockForOrderCommand> captor = ArgumentCaptor.forClass(ReserveStockForOrderCommand.class);
        verify(handler).execute(captor.capture());
        assertNotNull(captor.getValue());
        assertEquals(555L, captor.getValue().salesOrderId());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals(20L, captor.getValue().outletId());
        assertEquals(1, captor.getValue().items().size());
        assertEquals(100L, captor.getValue().items().getFirst().productId());
        assertEquals(3, captor.getValue().items().getFirst().quantity());
    }
}
