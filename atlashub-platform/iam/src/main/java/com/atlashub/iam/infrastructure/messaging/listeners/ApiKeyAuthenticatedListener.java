package com.atlashub.iam.infrastructure.messaging.listeners;

import com.atlashub.iam.application.commands.RecordApiKeyUsage.RecordApiKeyUsageCommand;
import com.atlashub.iam.application.commands.RecordApiKeyUsage.RecordApiKeyUsageHandler;
import com.atlashub.iam.infrastructure.messaging.events.ApiKeyAuthenticated;
import com.atlashub.iam.domain.events.ApiKeyAuthenticatedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class ApiKeyAuthenticatedListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthenticatedListener.class);
    private static final String GROUP_ID = "iam-api-key-usage-group";
    private final RecordApiKeyUsageHandler handler;

    public ApiKeyAuthenticatedListener(ObjectMapper objectMapper, RecordApiKeyUsageHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription(ApiKeyAuthenticatedEvent.class.getName(), GROUP_ID);
    }

    @KafkaListener(topics = "iam-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(payload, "ApiKeyAuthenticatedEvent", ApiKeyAuthenticated.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute(new RecordApiKeyUsageCommand(
                        event.payload().organizationId(), event.payload().publicKey())));
    }
}
