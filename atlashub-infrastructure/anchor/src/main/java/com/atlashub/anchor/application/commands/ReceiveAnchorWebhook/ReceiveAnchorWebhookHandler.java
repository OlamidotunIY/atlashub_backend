package com.atlashub.anchor.application.commands.ReceiveAnchorWebhook;

import com.atlashub.anchor.configuration.AnchorEnvironment;
import com.atlashub.anchor.dto.common.AnchorRelationship;
import com.atlashub.anchor.dto.common.AnchorResourceIdentifier;
import com.atlashub.anchor.dto.webhook.AnchorWebhookPayload;
import com.atlashub.anchor.exception.MalformedAnchorWebhookException;
import com.atlashub.anchor.infrastructure.messaging.AnchorWebhookEventPublisher;
import com.atlashub.anchor.infrastructure.messaging.events.AnchorWebhookReceivedEvent;
import com.atlashub.anchor.infrastructure.webhook.AnchorWebhookSignatureVerifier;
import com.atlashub.shared.application.usecase.Command;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Verifies, parses, and publishes one minimal Anchor provider event without business processing.
 */
@Component
@ConditionalOnProperty(prefix = "atlashub.integrations.anchor", name = "enabled", havingValue = "true")
public class ReceiveAnchorWebhookHandler extends Command<ReceiveAnchorWebhookCommand, Void> {
    private final AnchorWebhookSignatureVerifier signatureVerifier;
    private final ObjectMapper objectMapper;
    private final AnchorWebhookEventPublisher eventPublisher;

    public ReceiveAnchorWebhookHandler(AnchorWebhookSignatureVerifier signatureVerifier, ObjectMapper objectMapper, AnchorWebhookEventPublisher eventPublisher) {
        this.signatureVerifier = signatureVerifier;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Void execute(ReceiveAnchorWebhookCommand command) {
        signatureVerifier.verify(command.rawBody(), command.signature(), command.environment(), command.consumer());
        AnchorWebhookPayload payload = parse(command.rawBody());
        eventPublisher.publish(toEvent(command.environment(), command.consumer(), payload));
        return null;
    }

    private AnchorWebhookPayload parse(byte[] rawBody) {
        try {
            AnchorWebhookPayload payload = objectMapper.readValue(rawBody, AnchorWebhookPayload.class);
            if (payload.data() == null || isBlank(payload.data().id()) || isBlank(payload.data().type())) {
                throw new MalformedAnchorWebhookException("Anchor webhook is missing its event ID or type");
            }
            return payload;
        } catch (IOException exception) {
            throw new MalformedAnchorWebhookException("Anchor webhook payload is not valid JSON", exception);
        }
    }

    private AnchorWebhookReceivedEvent toEvent(
            AnchorEnvironment environment,
            com.atlashub.anchor.configuration.AnchorWebhookConsumer consumer,
            AnchorWebhookPayload payload
    ) {
        AnchorWebhookPayload.EventData data = payload.data();
        Map<String, AnchorResourceIdentifier> relationships = new LinkedHashMap<>();
        if (data.relationships() != null) {
            for (Map.Entry<String, AnchorRelationship> relationship : data.relationships().entrySet()) {
                if (relationship.getValue() != null && relationship.getValue().data() != null) {
                    relationships.put(relationship.getKey(), relationship.getValue().data());
                }
            }
        }
        String occurredAt = data.attributes() == null ? null : data.attributes().createdAt();
        return new AnchorWebhookReceivedEvent(data.id(), environment, consumer, data.type(), occurredAt, relationships);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
