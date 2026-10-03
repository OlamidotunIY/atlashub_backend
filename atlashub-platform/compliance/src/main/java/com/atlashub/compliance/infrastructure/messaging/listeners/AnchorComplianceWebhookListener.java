package com.atlashub.compliance.infrastructure.messaging.listeners;

import com.atlashub.anchor.configuration.AnchorWebhookConsumer;
import com.atlashub.anchor.dto.common.AnchorResourceIdentifier;
import com.atlashub.anchor.dto.common.AnchorIncludedResource;
import com.atlashub.anchor.infrastructure.messaging.events.AnchorWebhookReceivedEvent;
import com.atlashub.compliance.application.commands.RecordAnchorDecision.RecordAnchorDecisionCommand;
import com.atlashub.compliance.application.commands.RecordAnchorDecision.RecordAnchorDecisionHandler;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
public class AnchorComplianceWebhookListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(AnchorComplianceWebhookListener.class);
    private static final String GROUP_ID = "compliance-anchor-webhooks";
    private final RecordAnchorDecisionHandler handler;

    public AnchorComplianceWebhookListener(ObjectMapper objectMapper, RecordAnchorDecisionHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription(AnchorWebhookReceivedEvent.class.getName(), GROUP_ID);
    }

    @KafkaListener(topics = "anchor-events", groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, AnchorWebhookReceivedEvent.class.getSimpleName(),
                AnchorWebhookReceivedEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException, this::dispatch);
    }

    private void dispatch(AnchorWebhookReceivedEvent event) {
        if (event.consumer() != AnchorWebhookConsumer.COMPLIANCE) {
            return;
        }
        AnchorIncludedResource customer = event.includedResources().stream()
                .filter(value -> value.type() != null && value.type().toLowerCase().contains("customer"))
                .findFirst().orElse(null);
        Map<String, Object> attributes = customer == null ? event.attributes() : customer.attributes();
        String status = text(attributes, "status", "verificationStatus");
        if (status == null) {
            return;
        }
        boolean approved = "APPROVED".equalsIgnoreCase(status) || "VERIFIED".equalsIgnoreCase(status);
        boolean rejected = "REJECTED".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status);
        if (!approved && !rejected) {
            return;
        }
        String customerId = customer == null
                ? text(attributes, "customerId", "businessCustomerId") : customer.id();
        if (customerId == null) {
            customerId = relationshipId(event.relationships(), "customer", "businessCustomer");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("Anchor compliance webhook is missing customer id");
        }
        handler.execute(new RecordAnchorDecisionCommand(
                null, customerId, approved,
                rejected ? text(attributes, "reason", "failureReason", "message") : null));
    }

    private String relationshipId(Map<String, AnchorResourceIdentifier> relationships, String... names) {
        for (String name : names) {
            AnchorResourceIdentifier value = relationships.get(name);
            if (value != null && value.id() != null && !value.id().isBlank()) {
                return value.id();
            }
        }
        return null;
    }

    private String text(Map<String, Object> attributes, String... names) {
        for (String name : names) {
            Object value = attributes.get(name);
            if (value != null && !value.toString().isBlank()) {
                return value.toString();
            }
        }
        return null;
    }
}
