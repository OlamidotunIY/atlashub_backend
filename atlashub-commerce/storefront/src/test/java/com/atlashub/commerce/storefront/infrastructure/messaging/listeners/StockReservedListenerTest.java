package com.atlashub.commerce.storefront.infrastructure.messaging.listeners;

import com.atlashub.commerce.storefront.application.commands.HandleStockReserved.HandleStockReservedCommand;
import com.atlashub.commerce.storefront.application.commands.HandleStockReserved.HandleStockReservedHandler;
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
class StockReservedListenerTest {

    private ObjectMapper objectMapper;

    @Mock
    private HandleStockReservedHandler handler;

    @Mock
    private EventTrackerPort eventTrackerPort;

    @Mock
    private DeadLetterRepository deadLetterRepository;

    private StockReservedListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listener = new StockReservedListener(objectMapper, handler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTrackerPort);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", deadLetterRepository);
        doAnswer(invocation -> null).when(handler).execute(any(HandleStockReservedCommand.class));
        listener.init();
    }

    @Test
    @DisplayName("Should process StockReservedEvent and delegate to HandleStockReservedHandler")
    void shouldProcessEventAndCallHandler() {
        String jsonPayload = """
                {
                    "eventId": "evt-sr-1",
                    "eventType": "StockReservedEvent",
                    "occurredAt": "2026-10-10T12:00:00Z",
                    "payload": {
                        "salesOrderId": 888,
                        "organizationId": 10,
                        "outletId": 20
                    }
                }
                """;

        listener.listen(jsonPayload);

        ArgumentCaptor<HandleStockReservedCommand> captor = ArgumentCaptor.forClass(HandleStockReservedCommand.class);
        verify(handler).execute(captor.capture());
        assertNotNull(captor.getValue());
        assertEquals(888L, captor.getValue().salesOrderId());
    }
}
