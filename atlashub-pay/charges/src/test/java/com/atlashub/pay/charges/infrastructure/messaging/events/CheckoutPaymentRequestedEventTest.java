package com.atlashub.pay.charges.infrastructure.messaging.events;

import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckoutPaymentRequestedEventTest {

    @Test
    @DisplayName("Should resolve properties from nested payload")
    void shouldResolveFromNestedPayload() {
        CheckoutPaymentRequestedEvent.Payload payload = new CheckoutPaymentRequestedEvent.Payload(
                10L,
                ApiEnvironment.LIVE,
                "ORD-PAY-1",
                new BigDecimal("15000.00"),
                CurrencyCode.NGN,
                ChargeChannel.CARD,
                "buyer@example.com",
                "COMMERCE",
                "ORD-99",
                "CUST-1",
                null,
                Map.of("key", "val")
        );

        CheckoutPaymentRequestedEvent event = new CheckoutPaymentRequestedEvent(
                "evt-1",
                "CheckoutPaymentRequestedEvent",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                payload
        );

        assertEquals(10L, event.resolveOrganizationId());
        assertEquals(ApiEnvironment.LIVE, event.resolveEnvironment());
        assertEquals("ORD-PAY-1", event.resolveReference());
        assertEquals(new BigDecimal("15000.00"), event.resolveAmount());
        assertEquals(CurrencyCode.NGN, event.resolveCurrency());
        assertEquals(ChargeChannel.CARD, event.resolveChannel());
        assertEquals("buyer@example.com", event.resolveEmail());
        assertEquals("COMMERCE", event.resolveSourceSystem());
        assertEquals("ORD-99", event.resolveSourceReferenceId());
        assertEquals("CUST-1", event.resolveCustomerReferenceId());
        assertEquals("val", event.resolveMetadata().get("key"));
    }

    @Test
    @DisplayName("Should resolve properties from root when payload is null")
    void shouldResolveFromRootProperties() {
        CheckoutPaymentRequestedEvent event = new CheckoutPaymentRequestedEvent(
                "evt-2",
                "CheckoutPaymentRequestedEvent",
                20L,
                ApiEnvironment.TEST,
                "ORD-PAY-2",
                new BigDecimal("8000.00"),
                CurrencyCode.NGN,
                ChargeChannel.USSD,
                "user@example.com",
                "COMMERCE",
                "ORD-100",
                null,
                null,
                Map.of(),
                null
        );

        assertEquals(20L, event.resolveOrganizationId());
        assertEquals(ApiEnvironment.TEST, event.resolveEnvironment());
        assertEquals("ORD-PAY-2", event.resolveReference());
        assertEquals(new BigDecimal("8000.00"), event.resolveAmount());
        assertEquals(CurrencyCode.NGN, event.resolveCurrency());
        assertEquals(ChargeChannel.USSD, event.resolveChannel());
        assertEquals("user@example.com", event.resolveEmail());
    }

    @Test
    @DisplayName("Should provide default fallback values when fields are null")
    void shouldProvideDefaultFallbacks() {
        CheckoutPaymentRequestedEvent event = new CheckoutPaymentRequestedEvent(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );

        assertEquals(ApiEnvironment.TEST, event.resolveEnvironment());
        assertEquals(CurrencyCode.NGN, event.resolveCurrency());
        assertEquals(ChargeChannel.CARD, event.resolveChannel());
        assertEquals("COMMERCE", event.resolveSourceSystem());
        assertNotNull(event.resolveMetadata());
        assertTrue(event.resolveMetadata().isEmpty());
    }
}
