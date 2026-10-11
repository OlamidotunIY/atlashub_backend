package com.atlashub.audit.application.query;

import com.atlashub.audit.application.query.ListAuditLogs.ListAuditLogsHandler;
import com.atlashub.audit.application.query.ListAuditLogs.ListAuditLogsQuery;
import com.atlashub.audit.domain.entities.AuditLog;
import com.atlashub.audit.domain.repositories.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAuditLogsHandlerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private ListAuditLogsHandler handler;

    @Test
    @DisplayName("Should list audit logs without resourceType filter")
    void execute_withoutResourceType_queriesByOrg() {
        AuditLog log1 = AuditLog.create(1L, 10L, 20L, "ACT1", "TYPE1", "ID1", null, null, null, ZonedDateTime.now());
        when(auditLogRepository.findByOrganizationId(20L, 1, 20)).thenReturn(List.of(log1));

        List<AuditLogResult> results = handler.execute(new ListAuditLogsQuery(20L, null, 1, 20));

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).id());
        verify(auditLogRepository).findByOrganizationId(20L, 1, 20);
    }

    @Test
    @DisplayName("Should list audit logs with resourceType filter")
    void execute_withResourceType_queriesByOrgAndType() {
        AuditLog log1 = AuditLog.create(1L, 10L, 20L, "ACT1", "SESSION", "ID1", null, null, null, ZonedDateTime.now());
        when(auditLogRepository.findByOrganizationIdAndResourceType(20L, "SESSION", 1, 10)).thenReturn(List.of(log1));

        List<AuditLogResult> results = handler.execute(new ListAuditLogsQuery(20L, "SESSION", 1, 10));

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("SESSION", results.get(0).resourceType());
        verify(auditLogRepository).findByOrganizationIdAndResourceType(20L, "SESSION", 1, 10);
    }
}
