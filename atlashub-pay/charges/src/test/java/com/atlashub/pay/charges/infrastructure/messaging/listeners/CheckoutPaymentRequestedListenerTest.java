package com.atlashub.pay.charges.infrastructure.messaging.listeners;

import com.atlashub.pay.charges.application.commands.InitializeCharge.InitializeChargeCommand;
import com.atlashub.pay.charges.application.commands.InitializeCharge.InitializeChargeHandler;
import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.shared.application.port.EventTrackerPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
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

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutPaymentRequestedListenerTest {

    private static final String GROUP_ID = "pay-charges-commerce-checkout";

    @Mock
    private InitializeChargeHandler handler;

    @Mock
    private EventTrackerPort eventTrackerPort;

    @Mock
    private DeadLetterRepository deadLetterRepository;

    private CheckoutPaymentRequestedListener listener;

    @BeforeEach
    void setUp() {
        listener = new CheckoutPaymentRequestedListener(new ObjectMapper().findAndRegisterModules(), handler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTrackerPort);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", deadLetterRepository);
    }

    @Test
    @DisplayName("Should register subscription upon init")
    void shouldRegisterSubscriptionOnInit() {
        listener.init();
        verify(eventTrackerPort).registerSubscription("CheckoutPaymentRequestedEvent", GROUP_ID);
    }

    @Test
    @DisplayName("Should parse CheckoutPaymentRequestedEvent and execute InitializeChargeHandler")
    void shouldDispatchValidCheckoutPaymentRequestedEvent() {
        when(eventTrackerPort.isProcessed("evt-com-1", GROUP_ID)).thenReturn(false);

        String payload = """
                {
                  "eventType": "CheckoutPaymentRequestedEvent",
                  "correlationId": "evt-com-1",
                  "payload": {
                    "organizationId": 10,
                    "environment": "LIVE",
                    "reference": "ORD-12345",
                    "amount": 7500.00,
                    "currency": "NGN",
                    "channel": "CARD",
                    "email": "buyer@example.com",
                    "sourceSystem": "COMMERCE",
                    "sourceReferenceId": "ORDER-999"
                  }
                }
                """;

        listener.listen(payload);

        ArgumentCaptor<InitializeChargeCommand> captor = ArgumentCaptor.forClass(InitializeChargeCommand.class);
        verify(handler).execute(captor.capture());
        verify(eventTrackerPort).markSuccess("evt-com-1", GROUP_ID);

        InitializeChargeCommand command = captor.getValue();
        assertEquals(10L, command.organizationId());
        assertEquals(ApiEnvironment.LIVE, command.environment());
        assertEquals("ORD-12345", command.reference());
        assertEquals(0, command.amount().amount().compareTo(new BigDecimal("7500.00")));
        assertEquals(CurrencyCode.NGN, command.amount().currency());
        assertEquals(ChargeChannel.CARD, command.channel());
        assertEquals("buyer@example.com", command.email());
        assertEquals("COMMERCE", command.sourceSystem());
        assertEquals("ORDER-999", command.sourceReferenceId());
    }

    @Test
    @DisplayName("Should ignore malformed event missing reference or organization")
    void shouldIgnoreEventMissingRequiredFields() {
        when(eventTrackerPort.isProcessed("evt-com-2", GROUP_ID)).thenReturn(false);

        String payload = """
                {
                  "eventType": "CheckoutPaymentRequestedEvent",
                  "correlationId": "evt-com-2",
                  "payload": {
                    "organizationId": null,
                    "reference": null,
                    "amount": 500.00
                  }
                }
                """;

        listener.listen(payload);

        verify(handler, never()).execute(any());
        verify(eventTrackerPort).markSuccess("evt-com-2", GROUP_ID);
    }

    @Test
    @DisplayName("Should ignore non-matching event")
    void shouldIgnoreNonMatchingEvent() {
        String payload = """
                {
                  "eventType": "OtherCommerceEvent",
                  "correlationId": "evt-com-3",
                  "payload": {}
                }
                """;

        listener.listen(payload);

        verify(handler, never()).execute(any());
    }
}
