package com.atlashub.audit.adapter.in.messaging;

import com.atlashub.audit.application.command.LogActivityCommand;
import com.atlashub.audit.application.usecase.LogActivityUseCase;
import com.atlashub.audit.domain.valueobject.ActivityAction;
import com.atlashub.shared.api.EventTrackerApi;
import com.atlashub.shared.event.EnvelopedDomainEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class IdentityAuditEventListener {

    private final LogActivityUseCase logActivityUseCase;
    private final ObjectMapper objectMapper;
    private final EventTrackerApi eventTrackerApi;

    public IdentityAuditEventListener(LogActivityUseCase logActivityUseCase, ObjectMapper objectMapper, EventTrackerApi eventTrackerApi) {
        this.logActivityUseCase = logActivityUseCase;
        this.objectMapper = objectMapper;
        this.eventTrackerApi = eventTrackerApi;
    }

    @KafkaListener(topics = {"organization-events", "organization-member-events", "invitation-events", "user-events"}, groupId = "audit-group")
    public void onIdentityEvent(EnvelopedDomainEvent<?> envelopedEvent) {
        String eventId = envelopedEvent.correlationId();
        if (eventTrackerApi.isProcessed(eventId, "audit-group")) {
            return;
        }

        String eventType = envelopedEvent.eventType();
        
        ActivityAction action = switch (eventType) {
            case "UserCreated" -> ActivityAction.USER_REGISTERED;
            case "OrganizationCreated" -> ActivityAction.ORGANIZATION_CREATED;
            case "OrganizationMemberAdded" -> ActivityAction.MEMBER_JOINED;
            case "OrganizationMemberRemoved" -> ActivityAction.MEMBER_REMOVED;
            case "InvitationCreated" -> ActivityAction.INVITATION_SENT;
            case "InvitationAccepted" -> ActivityAction.INVITATION_ACCEPTED;
            case "ApiKeyCreated" -> ActivityAction.API_KEY_GENERATED;
            default -> null;
        };

        if (action == null) {
            eventTrackerApi.markSuccess(eventId, "audit-group");
            return;
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("correlationId", envelopedEvent.correlationId());
        metadata.put("eventId", envelopedEvent.event().eventId());
        
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
                } else if (payloadMap.containsKey("inviterId") && payloadMap.get("inviterId") != null) {
                    actorId = Long.valueOf(payloadMap.get("inviterId").toString());
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
        
        eventTrackerApi.markSuccess(eventId, "audit-group");
    }
}
