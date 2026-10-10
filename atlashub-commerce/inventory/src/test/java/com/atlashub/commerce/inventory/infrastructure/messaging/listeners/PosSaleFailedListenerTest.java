package com.atlashub.commerce.inventory.infrastructure.messaging.listeners;

import com.atlashub.commerce.inventory.application.commands.ReleaseReservedStock.ReleaseReservedStockCommand;
import com.atlashub.commerce.inventory.application.commands.ReleaseReservedStock.ReleaseReservedStockHandler;
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
class PosSaleFailedListenerTest {

    private ObjectMapper objectMapper;

    @Mock
    private ReleaseReservedStockHandler handler;

    @Mock
    private EventTrackerPort eventTrackerPort;

    @Mock
    private DeadLetterRepository deadLetterRepository;

    private PosSaleFailedListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listener = new PosSaleFailedListener(objectMapper, handler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTrackerPort);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", deadLetterRepository);
        doAnswer(invocation -> null).when(handler).execute(any(ReleaseReservedStockCommand.class));
        listener.init();
    }

    @Test
    @DisplayName("Should process PosSaleFailedEvent and delegate to ReleaseReservedStockHandler")
    void shouldProcessEventAndCallHandler() {
        String jsonPayload = """
                {
                    "eventId": "evt-fail-1",
                    "eventType": "PosSaleFailedEvent",
                    "occurredAt": "2026-10-10T12:00:00Z",
                    "payload": {
                        "salesOrderId": 888,
                        "organizationId": 10,
                        "reason": "Payment expired"
                    }
                }
                """;

        listener.listen(jsonPayload);

        ArgumentCaptor<ReleaseReservedStockCommand> captor = ArgumentCaptor.forClass(ReleaseReservedStockCommand.class);
        verify(handler).execute(captor.capture());
        assertNotNull(captor.getValue());
        assertEquals(888L, captor.getValue().salesOrderId());
    }
}
