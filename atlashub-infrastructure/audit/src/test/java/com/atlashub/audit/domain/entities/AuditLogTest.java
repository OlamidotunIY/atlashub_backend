package com.atlashub.audit.domain.entities;

import com.atlashub.audit.domain.exceptions.AuditInvariantViolationException;
import com.atlashub.audit.domain.exceptions.AuditLogImmutableException;
import com.atlashub.audit.domain.valueobject.AuditAction;
import com.atlashub.audit.domain.valueobject.AuditResourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuditLogTest {

    @Test
    @DisplayName("Should create audit log successfully with valid fields")
    void create_validFields_success() {
        ZonedDateTime now = ZonedDateTime.now();
        AuditLog auditLog = AuditLog.create(
                100L,
                200L,
                300L,
                AuditAction.AUTH_SESSION_CREATED.name(),
                AuditResourceType.SESSION.name(),
                "session-123",
                null,
                "{\"status\":\"ACTIVE\"}",
                "127.0.0.1",
                now
        );

        assertEquals(100L, auditLog.getId());
        assertEquals(200L, auditLog.getUserId());
        assertEquals(300L, auditLog.getOrganizationId());
        assertEquals(AuditAction.AUTH_SESSION_CREATED.name(), auditLog.getAction());
        assertEquals(AuditResourceType.SESSION.name(), auditLog.getResourceType());
        assertEquals("session-123", auditLog.getResourceId());
        assertEquals("{\"status\":\"ACTIVE\"}", auditLog.getNewState());
        assertEquals("127.0.0.1", auditLog.getIpAddress());
        assertEquals(now, auditLog.getTimestamp());
    }

    @Test
    @DisplayName("Should use current timestamp when null timestamp passed")
    void create_nullTimestamp_usesNow() {
        AuditLog auditLog = AuditLog.create(
                100L,
                null,
                300L,
                "SYSTEM_ACTION",
                "SYSTEM",
                "sys-1",
                null,
                null,
                null,
                null
        );

        assertNotNull(auditLog.getTimestamp());
    }

    @Test
    @DisplayName("Should throw AuditInvariantViolationException when ID is null")
    void create_nullId_throwsException() {
        assertThrows(AuditInvariantViolationException.class, () ->
                AuditLog.create(null, 1L, 1L, "ACTION", "TYPE", "RES-1", null, null, null, ZonedDateTime.now()));
    }

    @Test
    @DisplayName("Should throw AuditInvariantViolationException when action is blank")
    void create_blankAction_throwsException() {
        assertThrows(AuditInvariantViolationException.class, () ->
                AuditLog.create(1L, 1L, 1L, "  ", "TYPE", "RES-1", null, null, null, ZonedDateTime.now()));
    }

    @Test
    @DisplayName("Should throw AuditInvariantViolationException when resourceType is blank")
    void create_blankResourceType_throwsException() {
        assertThrows(AuditInvariantViolationException.class, () ->
                AuditLog.create(1L, 1L, 1L, "ACTION", "", "RES-1", null, null, null, ZonedDateTime.now()));
    }

    @Test
    @DisplayName("Should throw AuditInvariantViolationException when resourceId is blank")
    void create_blankResourceId_throwsException() {
        assertThrows(AuditInvariantViolationException.class, () ->
                AuditLog.create(1L, 1L, 1L, "ACTION", "TYPE", "  ", null, null, null, ZonedDateTime.now()));
    }

    @Test
    @DisplayName("Should throw AuditLogImmutableException on mutation attempt")
    void verifyImmutable_throwsException() {
        AuditLog auditLog = AuditLog.create(1L, 1L, 1L, "ACTION", "TYPE", "RES-1", null, null, null, ZonedDateTime.now());
        assertThrows(AuditLogImmutableException.class, auditLog::verifyImmutable);
    }
}
