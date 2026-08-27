package com.atlashub.audit.application.command;

import com.atlashub.audit.domain.valueobject.ActivityAction;
import java.util.Map;

public record LogActivityCommand(
    Long organizationId,
    Long actorUserId,
    ActivityAction action,
    String entityType,
    String entityId,
    Map<String, Object> metadata
) {}
