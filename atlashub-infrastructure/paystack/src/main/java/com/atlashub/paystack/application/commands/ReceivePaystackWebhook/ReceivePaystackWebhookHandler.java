package com.atlashub.paystack.application.commands.ReceivePaystackWebhook;

import com.atlashub.paystack.infrastructure.external.paystack.messaging.PaystackWebhookEventPublisher;
import com.atlashub.paystack.infrastructure.external.paystack.messaging.events.PaystackWebhookReceivedEvent;
import com.atlashub.paystack.infrastructure.external.paystack.dto.webhook.PaystackWebhookPayload;
import com.atlashub.paystack.infrastructure.external.paystack.webhook.PaystackWebhookSignatureVerifier;
import com.atlashub.paystack.exception.MalformedPaystackWebhookException;
import com.atlashub.shared.application.usecase.Command;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix="atlashub.integrations.paystack", name="enabled", havingValue="true")
public class ReceivePaystackWebhookHandler extends Command<ReceivePaystackWebhookCommand, Void> {
    private final PaystackWebhookSignatureVerifier verifier;
    private final ObjectMapper mapper;
    private final PaystackWebhookEventPublisher publisher;

    public ReceivePaystackWebhookHandler(PaystackWebhookSignatureVerifier verifier, ObjectMapper mapper,
                                         PaystackWebhookEventPublisher publisher) {
        this.verifier = verifier;
        this.mapper = mapper;
        this.publisher = publisher;
    }

    @Override
    public Void execute(ReceivePaystackWebhookCommand command) {
        verifier.verify(command.rawBody(), command.signature(), command.environment());
        try {
            PaystackWebhookPayload payload = mapper.readValue(command.rawBody(), PaystackWebhookPayload.class);
            if (payload.event() == null || payload.event().isBlank() || payload.data() == null || !payload.data().isObject())
                throw new MalformedPaystackWebhookException("Paystack webhook is missing event type or data");
            JsonNode idNode = payload.data().get("id");
            JsonNode referenceNode = payload.data().get("reference");
            String id = idNode != null && !idNode.isNull() ? idNode.asText() :
                    referenceNode == null || referenceNode.isNull() ? null : referenceNode.asText();
            if (id == null || id.isBlank())
                throw new MalformedPaystackWebhookException("Paystack webhook is missing event identity");
            Map<String,Object> data = mapper.convertValue(payload.data(),
                    mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
            publisher.publish(new PaystackWebhookReceivedEvent(id, command.environment(), payload.event(), data));
            return null;
        } catch (MalformedPaystackWebhookException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new MalformedPaystackWebhookException("Paystack webhook payload is not valid JSON", ex);
        }
    }
}
