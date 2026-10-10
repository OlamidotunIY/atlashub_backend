package com.atlashub.pay.charges.infrastructure.messaging.listeners;

import com.atlashub.pay.charges.application.commands.ApplyChargeProviderStatus.ApplyChargeProviderStatusCommand;
import com.atlashub.pay.charges.application.commands.ApplyChargeProviderStatus.ApplyChargeProviderStatusHandler;
import com.atlashub.pay.charges.application.commands.ApplyRefundProviderStatus.ApplyRefundProviderStatusHandler;
import com.atlashub.pay.charges.application.commands.ApplyRefundProviderStatus.ApplyRefundProviderStatusCommand;
import com.atlashub.pay.charges.application.commands.ApplyChargeDispute.ApplyChargeDisputeHandler;
import com.atlashub.shared.application.port.EventTrackerPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaystackChargeWebhookListenerTest {

    private static final String GROUP_ID = "pay-charges-paystack-webhooks";

    @Mock
    private ApplyChargeProviderStatusHandler handler;

    @Mock
    private ApplyRefundProviderStatusHandler refundHandler;

    @Mock
    private ApplyChargeDisputeHandler disputeHandler;

    @Mock
    private EventTrackerPort eventTrackerPort;

    @Mock
    private DeadLetterRepository deadLetterRepository;

    private PaystackChargeWebhookListener listener;

    @BeforeEach
    void setUp() {
        listener = new PaystackChargeWebhookListener(
                new ObjectMapper().findAndRegisterModules(), handler, refundHandler, disputeHandler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTrackerPort);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", deadLetterRepository);
    }

    @Test
    @DisplayName("Should register subscription upon init")
    void shouldRegisterSubscriptionOnInit() {
        listener.init();
        verify(eventTrackerPort).registerSubscription("PaystackWebhookReceivedEvent", GROUP_ID);
    }

    @Test
    @DisplayName("Should parse charge.success webhook and dispatch command to handler")
    void shouldDispatchSuccessfulChargeEvent() {
        when(eventTrackerPort.isProcessed("evt-100", GROUP_ID)).thenReturn(false);

        String payload = """
                {
                  "eventType": "PaystackWebhookReceivedEvent",
                  "correlationId": "evt-100",
                  "payload": {
                    "eventId": "evt-100",
                    "environment": "TEST",
                    "eventType": "charge.success",
                    "data": {
                      "reference": "REF-100",
                      "id": "GATEWAY-REF-100",
                      "amount": 500000,
                      "currency": "NGN"
                    }
                  }
                }
                """;

        listener.listen(payload);

        ArgumentCaptor<ApplyChargeProviderStatusCommand> captor =
                ArgumentCaptor.forClass(ApplyChargeProviderStatusCommand.class);
        verify(handler).execute(captor.capture());
        verify(eventTrackerPort).markSuccess("evt-100", GROUP_ID);

        ApplyChargeProviderStatusCommand command = captor.getValue();
        assertEquals(ApiEnvironment.TEST, command.environment());
        assertEquals("REF-100", command.reference());
        assertEquals("GATEWAY-REF-100", command.providerReference());
        assertEquals(Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN), command.confirmedAmount());
        assertEquals(CurrencyCode.NGN, command.confirmedCurrency());
        assertTrue(command.successful());
        assertNull(command.failureReason());
    }

    @Test
    void shouldDispatchProcessedRefundEvent() {
        when(eventTrackerPort.isProcessed("evt-refund-100", GROUP_ID)).thenReturn(false);
        String payload = """
                {
                  "eventType": "PaystackWebhookReceivedEvent",
                  "correlationId": "evt-refund-100",
                  "payload": {
                    "eventId": "evt-refund-100",
                    "environment": "TEST",
                    "eventType": "refund.processed",
                    "data": {
                      "transaction_reference": "PAYSTACK-REF-100",
                      "refund_reference": "REFUND-100",
                      "amount": "500000",
                      "currency": "NGN"
                    }
                  }
                }
                """;

        listener.listen(payload);

        ArgumentCaptor<ApplyRefundProviderStatusCommand> captor =
                ArgumentCaptor.forClass(ApplyRefundProviderStatusCommand.class);
        verify(refundHandler).execute(captor.capture());
        assertEquals("PAYSTACK-REF-100", captor.getValue().transactionReference());
        assertEquals("REFUND-100", captor.getValue().refundReference());
        assertEquals(0, new BigDecimal("5000.00").compareTo(captor.getValue().amount().amount()));
        assertEquals("processed", captor.getValue().status());
    }

    @Test
    @DisplayName("Should parse charge.failed webhook and dispatch failure command to handler")
    void shouldDispatchFailedChargeEvent() {
        when(eventTrackerPort.isProcessed("evt-200", GROUP_ID)).thenReturn(false);

        String payload = """
                {
                  "eventType": "PaystackWebhookReceivedEvent",
                  "correlationId": "evt-200",
                  "payload": {
                    "eventId": "evt-200",
                    "environment": "LIVE",
                    "eventType": "charge.failed",
                    "data": {
                      "reference": "REF-200",
                      "id": 99999,
                      "amount": 350000,
                      "currency": "NGN",
                      "gateway_response": "Insufficient funds in customer account"
                    }
                  }
                }
                """;

        listener.listen(payload);

        ArgumentCaptor<ApplyChargeProviderStatusCommand> captor =
                ArgumentCaptor.forClass(ApplyChargeProviderStatusCommand.class);
        verify(handler).execute(captor.capture());
        verify(eventTrackerPort).markSuccess("evt-200", GROUP_ID);

        ApplyChargeProviderStatusCommand command = captor.getValue();
        assertEquals(ApiEnvironment.LIVE, command.environment());
        assertEquals("REF-200", command.reference());
        assertEquals("99999", command.providerReference());
        assertEquals(Money.of(new BigDecimal("3500.00"), CurrencyCode.NGN), command.confirmedAmount());
        assertEquals(CurrencyCode.NGN, command.confirmedCurrency());
        assertFalse(command.successful());
        assertEquals("Insufficient funds in customer account", command.failureReason());
    }

    @Test
    @DisplayName("Should fallback to message property when gateway_response is missing on charge.failed")
    void shouldFallbackToMessageWhenGatewayResponseMissing() {
        when(eventTrackerPort.isProcessed("evt-300", GROUP_ID)).thenReturn(false);

        String payload = """
                {
                  "eventType": "PaystackWebhookReceivedEvent",
                  "correlationId": "evt-300",
                  "payload": {
                    "eventId": "evt-300",
                    "environment": "TEST",
                    "eventType": "charge.failed",
                    "data": {
                      "reference": "REF-300",
                      "amount": 100000,
                      "currency": "NGN",
                      "message": "Declined by issuing bank"
                    }
                  }
                }
                """;

        listener.listen(payload);

        ArgumentCaptor<ApplyChargeProviderStatusCommand> captor =
                ArgumentCaptor.forClass(ApplyChargeProviderStatusCommand.class);
        verify(handler).execute(captor.capture());

        ApplyChargeProviderStatusCommand command = captor.getValue();
        assertEquals("REF-300", command.reference());
        assertEquals("REF-300", command.providerReference());
        assertEquals("Declined by issuing bank", command.failureReason());
    }

    @Test
    @DisplayName("Should ignore non-charge event types like transfer.success")
    void shouldIgnoreNonChargeEvents() {
        when(eventTrackerPort.isProcessed("evt-400", GROUP_ID)).thenReturn(false);

        String payload = """
                {
                  "eventType": "PaystackWebhookReceivedEvent",
                  "correlationId": "evt-400",
                  "payload": {
                    "eventId": "evt-400",
                    "environment": "TEST",
                    "eventType": "transfer.success",
                    "data": {
                      "reference": "TRF-400"
                    }
                  }
                }
                """;

        listener.listen(payload);

        verify(handler, never()).execute(any());
        verify(eventTrackerPort).markSuccess("evt-400", GROUP_ID);
    }

    @Test
    @DisplayName("Should ignore messages that do not match PaystackWebhookReceivedEvent")
    void shouldIgnoreNonMatchingEvent() {
        String payload = """
                {
                  "eventType": "SomeOtherWebhookEvent",
                  "correlationId": "evt-500",
                  "payload": {}
                }
                """;

        listener.listen(payload);

        verify(handler, never()).execute(any());
    }
}
