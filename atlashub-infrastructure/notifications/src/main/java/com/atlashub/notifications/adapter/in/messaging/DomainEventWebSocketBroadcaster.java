package com.atlashub.notifications.adapter.in.messaging;

import com.atlashub.shared.event.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DomainEventWebSocketBroadcaster extends BaseKafkaEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public DomainEventWebSocketBroadcaster(ObjectMapper objectMapper, SimpMessagingTemplate messagingTemplate) {
        super(objectMapper);
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = {"accounts-events", "identity-events"}, groupId = "ws-notifications-group")
    public void onDomainEvent(String payload) {
        try {
            var root = objectMapper.readTree(payload);
            String aggregateId = root.path("aggregateId").asText(null);
            String eventType = root.path("eventType").asText("UnknownEvent");

            if (aggregateId != null) {
                log.info("Broadcasting event {} to user {}", eventType, aggregateId);
                // Send to /user/{userId}/queue/events
                messagingTemplate.convertAndSendToUser(aggregateId, "/queue/events", payload);
            }
        } catch (Exception e) {
            log.error("Failed to broadcast WebSocket event", e);
        }
    }
}
