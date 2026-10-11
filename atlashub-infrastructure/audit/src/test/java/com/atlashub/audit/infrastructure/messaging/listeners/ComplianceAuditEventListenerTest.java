package com.atlashub.audit.infrastructure.messaging.listeners;

import com.atlashub.audit.application.command.RecordAuditLog.RecordAuditLogCommand;
import com.atlashub.audit.application.command.RecordAuditLog.RecordAuditLogHandler;
import com.atlashub.audit.domain.valueobject.AuditAction;
import com.atlashub.audit.domain.valueobject.AuditResourceType;
import com.atlashub.shared.application.port.EventTrackerPort;
import com.atlashub.shared.infrastructure.persistence.repository.DeadLetterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplianceAuditEventListenerTest {

    private ObjectMapper objectMapper;

    @Mock
    private RecordAuditLogHandler recordAuditLogHandler;

    @Mock
    private EventTrackerPort eventTrackerPort;

    @Mock
    private DeadLetterRepository deadLetterRepository;

    private ComplianceAuditEventListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        listener = new ComplianceAuditEventListener(objectMapper, recordAuditLogHandler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTrackerPort);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", deadLetterRepository);
        listener.init();
    }

    @Test
    @DisplayName("Should process OrganizationComplianceApprovedEvent and record audit log")
    void receive_complianceApproved_callsHandler() {
        String json = """
                {
                    "eventType": "OrganizationComplianceApprovedEvent",
                    "event": {
                        "eventId": "evt-c1",
                        "organizationId": 50,
                        "approvedByUserId": 100,
                        "notes": "Verified CAC",
                        "occurredAt": "2026-10-11T10:00:00Z"
                    }
                }
                """;

        when(eventTrackerPort.isProcessed("evt-c1", "audit-compliance-group")).thenReturn(false);

        listener.receive(json);

        ArgumentCaptor<RecordAuditLogCommand> captor = ArgumentCaptor.forClass(RecordAuditLogCommand.class);
        verify(recordAuditLogHandler).execute(captor.capture());
        RecordAuditLogCommand command = captor.getValue();

        assertNotNull(command);
        assertEquals(100L, command.userId());
        assertEquals(50L, command.organizationId());
        assertEquals(AuditAction.ORGANIZATION_COMPLIANCE_APPROVED.name(), command.action());
        assertEquals(AuditResourceType.COMPLIANCE.name(), command.resourceType());
        assertEquals("50", command.resourceId());
    }

    @Test
    @DisplayName("Should process OrganizationBannedEvent and record audit log")
    void receive_orgBanned_callsHandler() {
        String json = """
                {
                    "eventType": "OrganizationBannedEvent",
                    "event": {
                        "eventId": "evt-c2",
                        "organizationId": 60,
                        "bannedByUserId": 101,
                        "reason": "Fraudulent activities",
                        "occurredAt": "2026-10-11T10:00:00Z"
                    }
                }
                """;

        when(eventTrackerPort.isProcessed("evt-c2", "audit-compliance-group")).thenReturn(false);

        listener.receive(json);

        ArgumentCaptor<RecordAuditLogCommand> captor = ArgumentCaptor.forClass(RecordAuditLogCommand.class);
        verify(recordAuditLogHandler).execute(captor.capture());
        RecordAuditLogCommand command = captor.getValue();

        assertNotNull(command);
        assertEquals(101L, command.userId());
        assertEquals(60L, command.organizationId());
        assertEquals(AuditAction.ORGANIZATION_BANNED.name(), command.action());
        assertEquals(AuditResourceType.ORGANIZATION.name(), command.resourceType());
        assertEquals("60", command.resourceId());
    }
}
