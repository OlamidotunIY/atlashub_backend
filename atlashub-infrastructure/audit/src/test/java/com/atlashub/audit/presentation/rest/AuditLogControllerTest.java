package com.atlashub.audit.presentation.rest;

import com.atlashub.audit.application.query.AuditLogResult;
import com.atlashub.audit.application.query.GetAuditLogById.GetAuditLogByIdHandler;
import com.atlashub.audit.application.query.GetAuditLogById.GetAuditLogByIdQuery;
import com.atlashub.audit.application.query.ListAuditLogs.ListAuditLogsHandler;
import com.atlashub.audit.application.query.ListAuditLogs.ListAuditLogsQuery;
import com.atlashub.audit.presentation.dto.AuditLogResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogControllerTest {

    @Mock
    private GetAuditLogByIdHandler getAuditLogByIdHandler;

    @Mock
    private ListAuditLogsHandler listAuditLogsHandler;

    private AuditLogController controller;
    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new AuditLogController(getAuditLogByIdHandler, listAuditLogsHandler);
        principal = new AuthenticatedPrincipal(10L, 20L, "TEST", "sess-1", "tok-1", ZonedDateTime.now().plusHours(1));
    }

    @Test
    @DisplayName("Should get audit log by ID and return response")
    void getAuditLogById_delegatesToHandler() {
        AuditLogResult result = new AuditLogResult(
                100L,
                10L,
                20L,
                "TEST_ACTION",
                "SESSION",
                "res-1",
                null,
                null,
                "127.0.0.1",
                ZonedDateTime.now()
        );

        when(getAuditLogByIdHandler.execute(any(GetAuditLogByIdQuery.class))).thenReturn(result);

        ResponseEntity<ApiResponse<AuditLogResponse>> response = controller.getAuditLogById(principal, 100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().data().id());
        assertEquals("TEST_ACTION", response.getBody().data().action());
        verify(getAuditLogByIdHandler).execute(new GetAuditLogByIdQuery(100L, 20L));
    }

    @Test
    @DisplayName("Should list audit logs and return response list")
    void listAuditLogs_delegatesToHandler() {
        AuditLogResult result = new AuditLogResult(
                100L,
                10L,
                20L,
                "TEST_ACTION",
                "SESSION",
                "res-1",
                null,
                null,
                "127.0.0.1",
                ZonedDateTime.now()
        );

        when(listAuditLogsHandler.execute(any(ListAuditLogsQuery.class))).thenReturn(List.of(result));

        ResponseEntity<ApiResponse<List<AuditLogResponse>>> response =
                controller.listAuditLogs(principal, "SESSION", 1, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().data().size());
        assertEquals("SESSION", response.getBody().data().get(0).resourceType());
        verify(listAuditLogsHandler).execute(new ListAuditLogsQuery(20L, "SESSION", 1, 20));
    }
}
