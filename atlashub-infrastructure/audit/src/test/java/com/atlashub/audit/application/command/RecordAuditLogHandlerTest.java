package com.atlashub.audit.application.command.RecordAuditLog;

import com.atlashub.audit.domain.entities.AuditLog;
import com.atlashub.audit.domain.repositories.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordAuditLogHandlerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private RecordAuditLogHandler handler;

    @Test
    @DisplayName("Should create and save audit log, returning result")
    void execute_recordsAuditLogSuccessfully() {
        RecordAuditLogCommand command = new RecordAuditLogCommand(
                10L,
                20L,
                "USER_LOGIN",
                "ACCOUNT",
                "10",
                null,
                "{\"ip\":\"127.0.0.1\"}",
                "127.0.0.1"
        );

        when(auditLogRepository.nextIdentity()).thenReturn(555L);
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog arg = invocation.getArgument(0);
            return arg;
        });

        RecordAuditLogResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(555L, result.id());
        assertEquals(10L, result.userId());
        assertEquals(20L, result.organizationId());
        assertEquals("USER_LOGIN", result.action());
        assertEquals("ACCOUNT", result.resourceType());
        assertEquals("10", result.resourceId());
        assertNotNull(result.timestamp());

        verify(auditLogRepository).nextIdentity();
        verify(auditLogRepository).save(any(AuditLog.class));
    }
}
