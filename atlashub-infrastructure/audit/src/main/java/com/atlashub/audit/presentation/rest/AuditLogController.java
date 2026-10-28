package com.atlashub.audit.presentation.rest;

import com.atlashub.audit.application.query.AuditLogResult;
import com.atlashub.audit.application.query.GetAuditLogById.GetAuditLogByIdHandler;
import com.atlashub.audit.application.query.GetAuditLogById.GetAuditLogByIdQuery;
import com.atlashub.audit.application.query.ListAuditLogs.ListAuditLogsHandler;
import com.atlashub.audit.application.query.ListAuditLogs.ListAuditLogsQuery;
import com.atlashub.audit.presentation.dto.AuditLogResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "Audit Logs", description = "Immutable security audit trails")
@Validated
public class AuditLogController {

    private final GetAuditLogByIdHandler getAuditLogByIdHandler;
    private final ListAuditLogsHandler listAuditLogsHandler;

    public AuditLogController(GetAuditLogByIdHandler getAuditLogByIdHandler,
                              ListAuditLogsHandler listAuditLogsHandler) {
        this.getAuditLogByIdHandler = getAuditLogByIdHandler;
        this.listAuditLogsHandler = listAuditLogsHandler;
    }

    @GetMapping(params = "id")
    @Operation(summary = "Get audit log by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLogById(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam @Positive Long id) {
        AuditLogResult result = getAuditLogByIdHandler.execute(
                new GetAuditLogByIdQuery(id, principal.activeOrganizationId()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Audit log retrieved", toResponse(result), null));
    }

    @GetMapping
    @Operation(summary = "List audit logs for organization", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> listAuditLogs(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) String resourceType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<AuditLogResult> results = listAuditLogsHandler.execute(
                new ListAuditLogsQuery(principal.activeOrganizationId(), resourceType, page, size));
        List<AuditLogResponse> responses = results.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(new ApiResponse<>(true, "Audit logs retrieved", responses, null));
    }

    private AuditLogResponse toResponse(AuditLogResult result) {
        return new AuditLogResponse(
                result.id(),
                result.userId(),
                result.organizationId(),
                result.action(),
                result.resourceType(),
                result.resourceId(),
                result.previousState(),
                result.newState(),
                result.ipAddress(),
                result.timestamp()
        );
    }
}
