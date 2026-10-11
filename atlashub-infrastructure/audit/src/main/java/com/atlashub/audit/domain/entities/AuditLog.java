package com.atlashub.audit.domain.entities;

import com.atlashub.audit.domain.exceptions.AuditInvariantViolationException;
import com.atlashub.audit.domain.exceptions.AuditLogImmutableException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class AuditLog extends AggregateRoot<Long> {

    private final Long id;
    private final Long userId;
    private final Long organizationId;
    private final String action;
    private final String resourceType;
    private final String resourceId;
    private final String previousState;
    private final String newState;
    private final String ipAddress;
    private final ZonedDateTime timestamp;

    public AuditLog(Long id,
                    Long userId,
                    Long organizationId,
                    String action,
                    String resourceType,
                    String resourceId,
                    String previousState,
                    String newState,
                    String ipAddress,
                    ZonedDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.organizationId = organizationId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.previousState = previousState;
        this.newState = newState;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
    }

    public static AuditLog create(Long id,
                                  Long userId,
                                  Long organizationId,
                                  String action,
                                  String resourceType,
                                  String resourceId,
                                  String previousState,
                                  String newState,
                                  String ipAddress,
                                  ZonedDateTime timestamp) {
        if (id == null) {
            throw new AuditInvariantViolationException("Audit log ID must not be null");
        }
        if (action == null || action.isBlank()) {
            throw new AuditInvariantViolationException("Audit log action must not be blank");
        }
        if (resourceType == null || resourceType.isBlank()) {
            throw new AuditInvariantViolationException("Audit log resource type must not be blank");
        }
        if (resourceId == null || resourceId.isBlank()) {
            throw new AuditInvariantViolationException("Audit log resource ID must not be blank");
        }
        ZonedDateTime effectiveTimestamp = timestamp != null ? timestamp : ZonedDateTime.now();
        return new AuditLog(id, userId, organizationId, action, resourceType, resourceId, previousState, newState, ipAddress, effectiveTimestamp);
    }

    public void verifyImmutable() {
        throw new AuditLogImmutableException();
    }

    @Override
    public Long getId() {
        return id;
    }
}
