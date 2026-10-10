package com.atlashub.commerce.inventory.infrastructure.messaging.listeners;

import com.atlashub.commerce.inventory.application.commands.ApproveCustomerReturn.ApproveCustomerReturnCommand;
import com.atlashub.commerce.inventory.application.commands.ApproveCustomerReturn.ApproveCustomerReturnHandler;
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
class CustomerReturnShipmentReceivedListenerTest {

    private ObjectMapper objectMapper;

    @Mock
    private ApproveCustomerReturnHandler handler;

    @Mock
    private EventTrackerPort eventTrackerPort;

    @Mock
    private DeadLetterRepository deadLetterRepository;

    private CustomerReturnShipmentReceivedListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listener = new CustomerReturnShipmentReceivedListener(objectMapper, handler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTrackerPort);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", deadLetterRepository);
        doAnswer(invocation -> null).when(handler).execute(any(ApproveCustomerReturnCommand.class));
        listener.init();
    }

    @Test
    @DisplayName("Should process ReturnShipmentReceivedEvent and delegate to ApproveCustomerReturnHandler")
    void shouldProcessEventAndCallHandler() {
        String jsonPayload = """
                {
                    "eventId": "evt-123",
                    "eventType": "ReturnShipmentReceivedEvent",
                    "occurredAt": "2026-10-10T12:00:00Z",
                    "payload": {
                        "returnShipmentId": 999,
                        "returnId": 42,
                        "salesOrderId": 100,
                        "organizationId": 10,
                        "outletId": 20,
                        "items": [
                            { "productId": 500, "quantity": 2 }
                        ]
                    }
                }
                """;

        listener.listen(jsonPayload);

        ArgumentCaptor<ApproveCustomerReturnCommand> captor = ArgumentCaptor.forClass(ApproveCustomerReturnCommand.class);
        verify(handler).execute(captor.capture());
        assertNotNull(captor.getValue());
        assertEquals(42L, captor.getValue().returnId());
    }
}
