package com.atlashub.commerce.storefront.infrastructure.messaging.listeners;

import com.atlashub.commerce.storefront.application.commands.CompletePayment.CompletePaymentCommand;
import com.atlashub.commerce.storefront.application.commands.CompletePayment.CompletePaymentHandler;
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
class ChargeSuccessfulListenerTest {

    private ObjectMapper objectMapper;

    @Mock
    private CompletePaymentHandler handler;

    @Mock
    private EventTrackerPort eventTrackerPort;

    @Mock
    private DeadLetterRepository deadLetterRepository;

    private ChargeSuccessfulListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listener = new ChargeSuccessfulListener(objectMapper, handler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTrackerPort);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", deadLetterRepository);
        doAnswer(invocation -> null).when(handler).execute(any(CompletePaymentCommand.class));
        listener.init();
    }

    @Test
    @DisplayName("Should process ChargeSuccessfulEvent and delegate to CompletePaymentHandler")
    void shouldProcessEventAndCallHandler() {
        String jsonPayload = """
                {
                    "eventId": "evt-chg-1",
                    "eventType": "ChargeSuccessfulEvent",
                    "occurredAt": "2026-10-10T12:00:00Z",
                    "payload": {
                        "chargeReference": "chg-ref-99",
                        "sourceReferenceId": "555"
                    }
                }
                """;

        listener.listen(jsonPayload);

        ArgumentCaptor<CompletePaymentCommand> captor = ArgumentCaptor.forClass(CompletePaymentCommand.class);
        verify(handler).execute(captor.capture());
        assertNotNull(captor.getValue());
        assertEquals(555L, captor.getValue().salesOrderId());
    }
}
