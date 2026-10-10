package com.atlashub.paystack.application.commands.ReceivePaystackWebhook;

import com.atlashub.paystack.exception.MalformedPaystackWebhookException;
import com.atlashub.paystack.infrastructure.external.paystack.configuration.PaystackEnvironment;
import com.atlashub.paystack.infrastructure.external.paystack.messaging.PaystackWebhookEventPublisher;
import com.atlashub.paystack.infrastructure.external.paystack.webhook.PaystackWebhookSignatureVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test; import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*; import static org.mockito.Mockito.*;

class ReceivePaystackWebhookHandlerTest {
    @Test void verifies_parses_and_publishes_provider_fact() {
        var verifier=mock(PaystackWebhookSignatureVerifier.class);var publisher=mock(PaystackWebhookEventPublisher.class);
        var handler=new ReceivePaystackWebhookHandler(verifier,new ObjectMapper(),publisher);
        byte[] body="{\"event\":\"charge.success\",\"data\":{\"id\":42,\"reference\":\"ref-1\"}}".getBytes(StandardCharsets.UTF_8);
        handler.execute(new ReceivePaystackWebhookCommand(PaystackEnvironment.TEST,body,"signature"));
        verify(verifier).verify(body,"signature",PaystackEnvironment.TEST);
        var captor=ArgumentCaptor.forClass(com.atlashub.paystack.infrastructure.external.paystack.messaging.events.PaystackWebhookReceivedEvent.class);verify(publisher).publish(captor.capture());assertEquals("charge.success",captor.getValue().eventType());assertEquals("42",captor.getValue().eventId());
    }
    @Test void rejects_payload_without_identity() {var handler=new ReceivePaystackWebhookHandler(mock(PaystackWebhookSignatureVerifier.class),new ObjectMapper(),mock(PaystackWebhookEventPublisher.class));byte[] body="{\"event\":\"charge.success\",\"data\":{}}".getBytes(StandardCharsets.UTF_8);assertThrows(MalformedPaystackWebhookException.class,()->handler.execute(new ReceivePaystackWebhookCommand(PaystackEnvironment.TEST,body,"signature")));}
}
