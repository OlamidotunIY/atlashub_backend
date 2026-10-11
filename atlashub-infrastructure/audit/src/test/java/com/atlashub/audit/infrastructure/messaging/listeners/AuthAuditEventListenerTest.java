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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthAuditEventListenerTest {

    private ObjectMapper objectMapper;

    @Mock
    private RecordAuditLogHandler recordAuditLogHandler;

    @Mock
    private EventTrackerPort eventTrackerPort;

    @Mock
    private DeadLetterRepository deadLetterRepository;

    private AuthAuditEventListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        listener = new AuthAuditEventListener(objectMapper, recordAuditLogHandler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTrackerPort);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", deadLetterRepository);
        listener.init();
    }

    @Test
    @DisplayName("Should process AuthSessionCreatedEvent and record audit log")
    void receive_authSessionCreatedEvent_callsHandler() {
        String json = """
                {
                    "eventType": "AuthSessionCreatedEvent",
                    "event": {
                        "eventId": "evt-1",
                        "aggregateId": 999,
                        "userId": 10,
                        "organizationId": 20,
                        "ipAddress": "192.168.1.1",
                        "occurredAt": "2026-10-11T10:00:00Z"
                    }
                }
                """;

        when(eventTrackerPort.isProcessed("evt-1", "audit-auth-group")).thenReturn(false);

        listener.receive(json);

        ArgumentCaptor<RecordAuditLogCommand> captor = ArgumentCaptor.forClass(RecordAuditLogCommand.class);
        verify(recordAuditLogHandler).execute(captor.capture());
        RecordAuditLogCommand command = captor.getValue();

        assertNotNull(command);
        assertEquals(10L, command.userId());
        assertEquals(20L, command.organizationId());
        assertEquals(AuditAction.AUTH_SESSION_CREATED.name(), command.action());
        assertEquals(AuditResourceType.SESSION.name(), command.resourceType());
        assertEquals("999", command.resourceId());
        assertEquals("192.168.1.1", command.ipAddress());
    }

    @Test
    @DisplayName("Should process AuthAccountLockedEvent and record audit log")
    void receive_authAccountLockedEvent_callsHandler() {
        String json = """
                {
                    "eventType": "AuthAccountLockedEvent",
                    "event": {
                        "eventId": "evt-2",
                        "aggregateId": 888,
                        "userId": 15,
                        "organizationId": 25,
                        "reason": "Too many failed attempts",
                        "occurredAt": "2026-10-11T10:00:00Z"
                    }
                }
                """;

        when(eventTrackerPort.isProcessed("evt-2", "audit-auth-group")).thenReturn(false);

        listener.receive(json);

        ArgumentCaptor<RecordAuditLogCommand> captor = ArgumentCaptor.forClass(RecordAuditLogCommand.class);
        verify(recordAuditLogHandler).execute(captor.capture());
        RecordAuditLogCommand command = captor.getValue();

        assertNotNull(command);
        assertEquals(15L, command.userId());
        assertEquals(25L, command.organizationId());
        assertEquals(AuditAction.AUTH_ACCOUNT_LOCKED.name(), command.action());
        assertEquals(AuditResourceType.ACCOUNT.name(), command.resourceType());
        assertEquals("888", command.resourceId());
    }
}
