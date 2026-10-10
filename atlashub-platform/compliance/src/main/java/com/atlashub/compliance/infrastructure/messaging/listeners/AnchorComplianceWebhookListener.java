package com.atlashub.compliance.infrastructure.messaging.listeners;

import com.atlashub.compliance.application.commands.ApplyAnchorWebhook.ApplyAnchorWebhookCommand;
import com.atlashub.compliance.application.commands.ApplyAnchorWebhook.ApplyAnchorWebhookHandler;
import com.atlashub.compliance.infrastructure.messaging.events.AnchorWebhookReceivedEvent;
import com.atlashub.compliance.infrastructure.messaging.events.AnchorWebhookReceivedEvent.IncludedResource;
import com.atlashub.compliance.infrastructure.messaging.events.AnchorWebhookReceivedEvent.ResourceIdentifier;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpClientErrorException;

@Component
@ConditionalOnProperty(prefix = "atlashub.integrations.anchor", name = "enabled", havingValue = "true")
public class AnchorComplianceWebhookListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(AnchorComplianceWebhookListener.class);
    private static final String GROUP_ID = "compliance-anchor-webhooks";
    private final ApplyAnchorWebhookHandler handler;
    public AnchorComplianceWebhookListener(ObjectMapper mapper, ApplyAnchorWebhookHandler handler) {
        super(mapper); this.handler = handler;
    }
    @PostConstruct
    public void init() {
        registerSubscription(
                "com.atlashub.anchor.infrastructure.external.anchor.messaging.events.AnchorWebhookReceivedEvent",
                GROUP_ID
        );
    }
    @KafkaListener(topics = "anchor-events", groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, AnchorWebhookReceivedEvent.class.getSimpleName(), AnchorWebhookReceivedEvent.class,
                log, GROUP_ID, this::retryable, this::dispatch);
    }

    private void dispatch(AnchorWebhookReceivedEvent event) {
        if (!"COMPLIANCE".equalsIgnoreCase(event.consumer())
                || !"LIVE".equalsIgnoreCase(event.environment())) {
            return;
        }
        String customerId = relationshipId(event.relationships(), "customer", "businessCustomer");
        String documentId = relationshipId(event.relationships(), "document");
        for (IncludedResource included : event.includedResources()) {
            String type = included.type() == null ? "" : included.type().toLowerCase();
            if (customerId == null && type.contains("customer")) customerId = included.id();
            if (documentId == null && type.contains("document")) documentId = included.id();
        }
        if (customerId == null) customerId = text(event.attributes(), "customerId", "businessCustomerId");
        if (documentId == null) documentId = text(event.attributes(), "documentId");
        handler.execute(new ApplyAnchorWebhookCommand(event.eventType(), customerId, documentId,
                text(event.attributes(), "reason", "failureReason", "message"),
                text(event.attributes(), "code", "failureCode")));
    }

    private String relationshipId(Map<String, ResourceIdentifier> relationships, String... names) {
        for (String name : names) {
            ResourceIdentifier value = relationships.get(name);
            if (value != null && value.id() != null && !value.id().isBlank()) return value.id();
        }
        return null;
    }
    private String text(Map<String, Object> attributes, String... names) {
        for (String name : names) {
            Object value = attributes.get(name);
            if (value != null && !value.toString().isBlank()) return value.toString();
        }
        return null;
    }
    private boolean retryable(Exception error) {
        Throwable cause = error;
        while (cause != null) {
            if (cause instanceof TimeoutException || cause instanceof ResourceAccessException
                    || cause instanceof HttpServerErrorException
                    || cause instanceof HttpClientErrorException.TooManyRequests) return true;
            cause = cause.getCause();
        }
        return false;
    }
}
