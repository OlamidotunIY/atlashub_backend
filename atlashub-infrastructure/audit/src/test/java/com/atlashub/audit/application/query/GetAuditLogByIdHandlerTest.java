package com.atlashub.audit.application.query;

import com.atlashub.audit.application.query.GetAuditLogById.GetAuditLogByIdHandler;
import com.atlashub.audit.application.query.GetAuditLogById.GetAuditLogByIdQuery;
import com.atlashub.audit.domain.entities.AuditLog;
import com.atlashub.audit.domain.exceptions.AuditLogNotFoundException;
import com.atlashub.audit.domain.repositories.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAuditLogByIdHandlerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private GetAuditLogByIdHandler handler;

    @Test
    @DisplayName("Should return audit log result when found and org matches")
    void execute_foundAndOrgMatches_returnsResult() {
        ZonedDateTime now = ZonedDateTime.now();
        AuditLog auditLog = AuditLog.create(
                100L,
                10L,
                20L,
                "TEST_ACTION",
                "TEST_RESOURCE",
                "res-1",
                null,
                null,
                "127.0.0.1",
                now
        );

        when(auditLogRepository.findById(100L)).thenReturn(Optional.of(auditLog));

        AuditLogResult result = handler.execute(new GetAuditLogByIdQuery(100L, 20L));

        assertNotNull(result);
        assertEquals(100L, result.id());
        assertEquals(20L, result.organizationId());
        assertEquals("TEST_ACTION", result.action());
        verify(auditLogRepository).findById(100L);
    }

    @Test
    @DisplayName("Should throw AuditLogNotFoundException when not found")
    void execute_notFound_throwsException() {
        when(auditLogRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AuditLogNotFoundException.class, () ->
                handler.execute(new GetAuditLogByIdQuery(999L, 20L)));
    }

    @Test
    @DisplayName("Should throw AuditLogNotFoundException when org does not match")
    void execute_wrongOrg_throwsException() {
        AuditLog auditLog = AuditLog.create(
                100L,
                10L,
                999L,
                "TEST_ACTION",
                "TEST_RESOURCE",
                "res-1",
                null,
                null,
                "127.0.0.1",
                ZonedDateTime.now()
        );

        when(auditLogRepository.findById(100L)).thenReturn(Optional.of(auditLog));

        assertThrows(AuditLogNotFoundException.class, () ->
                handler.execute(new GetAuditLogByIdQuery(100L, 20L)));
    }
}
