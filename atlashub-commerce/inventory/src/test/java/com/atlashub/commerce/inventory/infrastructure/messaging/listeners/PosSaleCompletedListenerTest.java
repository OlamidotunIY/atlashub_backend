package com.atlashub.commerce.inventory.infrastructure.messaging.listeners;

import com.atlashub.commerce.inventory.application.commands.DeductReservedStock.DeductReservedStockCommand;
import com.atlashub.commerce.inventory.application.commands.DeductReservedStock.DeductReservedStockHandler;
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
class PosSaleCompletedListenerTest {

    private ObjectMapper objectMapper;

    @Mock
    private DeductReservedStockHandler handler;

    @Mock
    private EventTrackerPort eventTrackerPort;

    @Mock
    private DeadLetterRepository deadLetterRepository;

    private PosSaleCompletedListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listener = new PosSaleCompletedListener(objectMapper, handler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTrackerPort);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", deadLetterRepository);
        doAnswer(invocation -> null).when(handler).execute(any(DeductReservedStockCommand.class));
        listener.init();
    }

    @Test
    @DisplayName("Should process PosSaleCompletedEvent and delegate to DeductReservedStockHandler")
    void shouldProcessEventAndCallHandler() {
        String jsonPayload = """
                {
                    "eventId": "evt-sale-1",
                    "eventType": "PosSaleCompletedEvent",
                    "occurredAt": "2026-10-10T12:00:00Z",
                    "payload": {
                        "salesOrderId": 777,
                        "organizationId": 10,
                        "outletId": 20
                    }
                }
                """;

        listener.listen(jsonPayload);

        ArgumentCaptor<DeductReservedStockCommand> captor = ArgumentCaptor.forClass(DeductReservedStockCommand.class);
        verify(handler).execute(captor.capture());
        assertNotNull(captor.getValue());
        assertEquals(777L, captor.getValue().salesOrderId());
    }
}
