package com.atlashub.anchor.infrastructure.external.anchor.messaging;

import com.atlashub.anchor.infrastructure.external.anchor.configuration.AnchorProperties;
import com.atlashub.anchor.infrastructure.external.anchor.messaging.events.AnchorEventEnvelope;
import com.atlashub.anchor.infrastructure.external.anchor.messaging.events.AnchorWebhookReceivedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Publishes verified provider facts only after Kafka acknowledges receipt. */
@Component
@ConditionalOnProperty(prefix = "atlashub.integrations.anchor", name = "enabled", havingValue = "true")
public class AnchorWebhookEventPublisher {
    public static final String TOPIC = "anchor-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AnchorProperties properties;

    public AnchorWebhookEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            AnchorProperties properties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public void publish(AnchorWebhookReceivedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(new AnchorEventEnvelope(
                    AnchorWebhookReceivedEvent.class.getSimpleName(), event.eventId(), event
            ));
            kafkaTemplate.send(TOPIC, event.eventId(), payload)
                    .get(properties.webhookPublishTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (JsonProcessingException | InterruptedException | ExecutionException | TimeoutException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Anchor webhook could not be published", exception);
        }
    }
}
