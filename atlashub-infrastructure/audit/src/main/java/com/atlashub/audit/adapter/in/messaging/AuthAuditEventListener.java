package com.atlashub.audit.adapter.in.messaging;

import com.atlashub.audit.application.command.LogActivityCommand;
import com.atlashub.audit.application.usecase.LogActivityUseCase;
import com.atlashub.audit.domain.valueobject.ActivityAction;
import com.atlashub.shared.event.EnvelopedDomainEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuthAuditEventListener {

    private final LogActivityUseCase logActivityUseCase;
    private final ObjectMapper objectMapper;

    public AuthAuditEventListener(LogActivityUseCase logActivityUseCase, ObjectMapper objectMapper) {
        this.logActivityUseCase = logActivityUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "auth-events", groupId = "audit-group")
    public void onAuthEvent(EnvelopedDomainEvent<?> envelopedEvent) {
        String eventType = envelopedEvent.eventType();
        
        ActivityAction action;
        if ("AuthAccountCreated".equals(eventType)) {
            action = ActivityAction.USER_LOGIN;
        } else {
            return;
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("correlationId", envelopedEvent.correlationId());
        
        Long orgId = null;
        Long actorId = null;

        try {
            Map<String, Object> payloadMap = objectMapper.convertValue(envelopedEvent.event().payload(), new TypeReference<>() {});
            if (payloadMap != null) {
                metadata.putAll(payloadMap);
                if (payloadMap.containsKey("organizationId") && payloadMap.get("organizationId") != null) {
                    orgId = Long.valueOf(payloadMap.get("organizationId").toString());
                } else if (payloadMap.containsKey("integration") && payloadMap.get("integration") != null) {
                    orgId = Long.valueOf(payloadMap.get("integration").toString());
                }
                
                if (payloadMap.containsKey("userId") && payloadMap.get("userId") != null) {
                    actorId = Long.valueOf(payloadMap.get("userId").toString());
                } else if (payloadMap.containsKey("actorUserId") && payloadMap.get("actorUserId") != null) {
                    actorId = Long.valueOf(payloadMap.get("actorUserId").toString());
                }
            }
        } catch (Exception e) {
            metadata.put("payload_parsing_error", e.getMessage());
        }

        LogActivityCommand command = new LogActivityCommand(
            orgId,
            actorId,
            action,
            eventType,
            envelopedEvent.event().aggregateId(),
            metadata
        );
        logActivityUseCase.execute(command);
    }
}
