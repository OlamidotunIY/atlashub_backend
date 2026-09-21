package com.atlashub.audit.domain.model;

import com.atlashub.audit.domain.valueobject.ActivityAction;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
public class ActivityLog extends AggregateRoot<UUID> {
    
    private UUID id;
    private Long organizationId;
    private Long actorUserId;
    private ActivityAction action;
    private String entityType;
    private String entityId;
    private Map<String, Object> metadata;
    private ZonedDateTime occurredAt;

    protected ActivityLog() {}

    public ActivityLog(Long organizationId, Long actorUserId, ActivityAction action, String entityType, String entityId, Map<String, Object> metadata) {
        this.id = UUID.randomUUID();
        this.organizationId = organizationId;
        this.actorUserId = actorUserId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.metadata = metadata;
        this.occurredAt = ZonedDateTime.now();
    }

    public static ActivityLog reconstitute(UUID id, Long organizationId, Long actorUserId, ActivityAction action, String entityType, String entityId, Map<String, Object> metadata, ZonedDateTime occurredAt) {
        ActivityLog log = new ActivityLog();
        log.id = id;
        log.organizationId = organizationId;
        log.actorUserId = actorUserId;
        log.action = action;
        log.entityType = entityType;
        log.entityId = entityId;
        log.metadata = metadata;
        log.occurredAt = occurredAt;
        return log;
    }

    @Override
    public UUID getId() {
        return id;
    }
}
