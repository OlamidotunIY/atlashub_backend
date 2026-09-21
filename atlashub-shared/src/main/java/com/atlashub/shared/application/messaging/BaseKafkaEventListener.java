package com.atlashub.shared.application.messaging;

import com.atlashub.shared.infrastructure.persistence.repository.DeadLetterRepository;
import com.atlashub.shared.application.port.EventTrackerPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class BaseKafkaEventListener {

    protected final ObjectMapper objectMapper;
    
    @Autowired
    protected DeadLetterRepository deadLetterRepository;

    @Autowired
    protected EventTrackerPort EventTrackerPort;
    
    protected BaseKafkaEventListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @DltHandler
    public void handleDlt(String payload, 
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                          @Header(KafkaHeaders.GROUP_ID) String groupId,
                          @Header(name = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String exceptionMessage) {
        
        // Remove "-dlt" from topic name to get the original topic
        String originalTopic = topic.endsWith("-dlt") ? topic.substring(0, topic.length() - 4) : topic;
        
        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventId = root.path("correlationId").asText(null);
            if (eventId == null && root.has("event")) {
                eventId = root.get("event").path("eventId").asText(null);
            }
            if (eventId != null) {
                EventTrackerPort.markDlq(eventId, groupId);
            }
        } catch (Exception e) {
            // ignore JSON errors in DLT handler
        }

        deadLetterRepository.save(
                UUID.randomUUID().toString(),
                originalTopic,
                payload,
                exceptionMessage != null ? exceptionMessage : "Unknown error",
                ZonedDateTime.now()
        );
    }

    protected void processEventIfMatches(String messagePayload, String expectedEventType, Logger log, String groupId, Consumer<JsonNode> action) {
        try {
            if (!messagePayload.contains("\"" + expectedEventType + "\"") && 
                !messagePayload.contains("\"eventType\":\"" + expectedEventType + "\"")) {
                return;
            }

            JsonNode root = objectMapper.readTree(messagePayload);
            String actualType = root.path("eventType").asText(null);

            if (actualType != null && !actualType.endsWith(expectedEventType)) {
                return;
            }

            String eventId = root.path("correlationId").asText(null);
            if (eventId == null && root.has("event")) {
                eventId = root.get("event").path("eventId").asText(null);
            }

            if (eventId != null && EventTrackerPort.isProcessed(eventId, groupId)) {
                log.debug("Event {} already processed by {}, skipping.", eventId, groupId);
                return;
            }

            JsonNode eventNode = root.has("event") ? root.get("event") : root;
            action.accept(eventNode);

            if (eventId != null) {
                EventTrackerPort.markSuccess(eventId, groupId);
            }
        } catch (Exception e) {
            log.error("Failed to parse or process event. Expected type: {}. Payload: {}", expectedEventType, messagePayload, e);
            throw new RuntimeException("Error processing Kafka event", e);
        }
    }

    protected <T> void processEventIfMatches(String messagePayload, String expectedEventType, Class<T> payloadType, Logger log, String groupId, Consumer<T> action) {
        processEventIfMatches(messagePayload, expectedEventType, log, groupId, eventNode -> {
            try {
                T event = objectMapper.treeToValue(eventNode, payloadType);
                action.accept(event);
            } catch (Exception e) {
                log.error("Failed to deserialise or process event {}. Expected type: {}. Payload: {}", payloadType.getSimpleName(), expectedEventType, messagePayload, e);
                throw new RuntimeException("Error processing Kafka event: " + expectedEventType, e);
            }
        });
    }
}
