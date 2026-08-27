package com.atlashub.audit.application.result;

import com.atlashub.audit.domain.valueobject.ActivityAction;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

public record ActivityLogDto(
    UUID id,
    Long organizationId,
    Long actorUserId,
    ActivityAction action,
    String entityType,
    String entityId,
    Map<String, Object> metadata,
    ZonedDateTime occurredAt
) {}
