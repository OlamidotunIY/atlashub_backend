package com.atlashub.eventbus.application.usecase;

import com.atlashub.eventbus.application.command.ProcessOutboxMessagesCommand;
import com.atlashub.eventbus.application.port.EventTrackerPort;
import com.atlashub.eventbus.application.port.MessageBrokerPort;
import com.atlashub.eventbus.domain.model.OutboxMessage;
import com.atlashub.eventbus.domain.repository.OutboxMessageRepository;
import com.atlashub.shared.usecase.BaseUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ProcessOutboxMessagesUseCase extends BaseUseCase<ProcessOutboxMessagesCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(ProcessOutboxMessagesUseCase.class);
    private final OutboxMessageRepository repository;
    private final MessageBrokerPort messageBrokerPort;
    private final EventTrackerPort eventTrackerPort;
    private final ObjectMapper objectMapper;

    public ProcessOutboxMessagesUseCase(OutboxMessageRepository repository, MessageBrokerPort messageBrokerPort, EventTrackerPort eventTrackerPort, ObjectMapper objectMapper) {
        this.repository = repository;
        this.messageBrokerPort = messageBrokerPort;
        this.eventTrackerPort = eventTrackerPort;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Void execute(ProcessOutboxMessagesCommand command) {
        List<OutboxMessage> messages = repository.findPendingMessagesBatch(command.batchSize());
        
        if (messages.isEmpty()) {
            return null;
        }

        log.debug("Processing {} pending outbox messages", messages.size());

        for (OutboxMessage message : messages) {
            try {
                // Parse payload to find eventType and eventId
                JsonNode root = objectMapper.readTree(message.getPayload());
                String eventType = root.path("eventType").asText(null);
                
                String eventId = root.path("correlationId").asText(null);
                if (eventId == null && root.has("event")) {
                    eventId = root.get("event").path("eventId").asText(null);
                }

                if (eventType != null && eventId != null) {
                    Set<String> expectedConsumers = eventTrackerPort.getExpectedConsumers(eventType);
                    if (!expectedConsumers.isEmpty()) {
                        eventTrackerPort.createPendingTrackers(eventId, expectedConsumers);
                    }
                }

                messageBrokerPort.send(message.getTopic(), message.getId(), message.getPayload());
                message.markAsSent();
            } catch (Exception e) {
                log.error("Failed to process outbox message {}", message.getId(), e);
                message.markAsFailed();
            }
        }

        repository.saveAll(messages);
        return null;
    }
}
