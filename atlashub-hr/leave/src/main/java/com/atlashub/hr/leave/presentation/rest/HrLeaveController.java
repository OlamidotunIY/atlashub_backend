package com.atlashub.hr.leave.presentation.rest;

import com.atlashub.hr.leave.application.command.ApplyForLeave.ApplyForLeaveCommand;
import com.atlashub.hr.leave.application.command.ApplyForLeave.ApplyForLeaveHandler;
import com.atlashub.hr.leave.application.command.ApproveLeave.ApproveLeaveCommand;
import com.atlashub.hr.leave.application.command.ApproveLeave.ApproveLeaveHandler;
import com.atlashub.hr.leave.application.command.CancelLeave.CancelLeaveCommand;
import com.atlashub.hr.leave.application.command.CancelLeave.CancelLeaveHandler;
import com.atlashub.hr.leave.application.command.CreateLeaveType.CreateLeaveTypeCommand;
import com.atlashub.hr.leave.application.command.CreateLeaveType.CreateLeaveTypeHandler;
import com.atlashub.hr.leave.application.command.RejectLeave.RejectLeaveCommand;
import com.atlashub.hr.leave.application.command.RejectLeave.RejectLeaveHandler;
import com.atlashub.hr.leave.application.query.GetLeaveBalance.GetLeaveBalanceHandler;
import com.atlashub.hr.leave.application.query.GetLeaveBalance.GetLeaveBalanceQuery;
import com.atlashub.hr.leave.application.query.GetLeaveBalance.LeaveBalanceResult;
import com.atlashub.hr.leave.application.query.ListLeaveApplications.LeaveApplicationResult;
import com.atlashub.hr.leave.application.query.ListLeaveApplications.ListLeaveApplicationsHandler;
import com.atlashub.hr.leave.application.query.ListLeaveApplications.ListLeaveApplicationsQuery;
import com.atlashub.hr.leave.domain.valueobject.LeaveStatus;
import com.atlashub.hr.leave.presentation.dto.ApplyForLeaveRequest;
import com.atlashub.hr.leave.presentation.dto.ApplyForLeaveResponse;
import com.atlashub.hr.leave.presentation.dto.CreateLeaveTypeRequest;
import com.atlashub.hr.leave.presentation.dto.CreateLeaveTypeResponse;
import com.atlashub.hr.leave.presentation.dto.RejectLeaveRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "HR Leave", description = "Endpoints for leave types, applications, and balances")
public class HrLeaveController {

    private final CreateLeaveTypeHandler createLeaveTypeHandler;
    private final ApplyForLeaveHandler applyForLeaveHandler;
    private final ApproveLeaveHandler approveLeaveHandler;
    private final RejectLeaveHandler rejectLeaveHandler;
    private final CancelLeaveHandler cancelLeaveHandler;
    private final GetLeaveBalanceHandler getLeaveBalanceHandler;
    private final ListLeaveApplicationsHandler listLeaveApplicationsHandler;

    public HrLeaveController(
            CreateLeaveTypeHandler createLeaveTypeHandler,
            ApplyForLeaveHandler applyForLeaveHandler,
            ApproveLeaveHandler approveLeaveHandler,
            RejectLeaveHandler rejectLeaveHandler,
            CancelLeaveHandler cancelLeaveHandler,
            GetLeaveBalanceHandler getLeaveBalanceHandler,
            ListLeaveApplicationsHandler listLeaveApplicationsHandler
    ) {
        this.createLeaveTypeHandler = createLeaveTypeHandler;
        this.applyForLeaveHandler = applyForLeaveHandler;
        this.approveLeaveHandler = approveLeaveHandler;
        this.rejectLeaveHandler = rejectLeaveHandler;
        this.cancelLeaveHandler = cancelLeaveHandler;
        this.getLeaveBalanceHandler = getLeaveBalanceHandler;
        this.listLeaveApplicationsHandler = listLeaveApplicationsHandler;
    }

    @PostMapping("/leave-types")
    @PreAuthorize("hasAuthority('hr:employees:manage')")
    @Operation(summary = "Create a new leave type", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CreateLeaveTypeResponse>> createLeaveType(
            @Valid @RequestBody CreateLeaveTypeRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long organizationId = request.organizationId() != null
                ? request.organizationId()
                : (principal != null ? principal.activeOrganizationId() : null);

        var result = createLeaveTypeHandler.execute(new CreateLeaveTypeCommand(
                organizationId,
                request.name(),
                request.maxDaysPerYear(),
                Boolean.TRUE.equals(request.isPaid()),
                Boolean.TRUE.equals(request.requiresApproval()),
                Boolean.TRUE.equals(request.carryOverAllowed()),
                request.maxCarryOverDays() != null ? request.maxCarryOverDays() : 0
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Leave type created successfully", new CreateLeaveTypeResponse(result.leaveTypeId()), null));
    }

    @PostMapping("/leave/apply")
    @Operation(summary = "Apply for leave", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ApplyForLeaveResponse>> applyForLeave(
            @Valid @RequestBody ApplyForLeaveRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long organizationId = principal != null ? principal.activeOrganizationId() : null;

        var result = applyForLeaveHandler.execute(new ApplyForLeaveCommand(
                request.employeeId(),
                organizationId,
                request.leaveTypeId(),
                request.startDate(),
                request.endDate(),
                request.reason()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Leave applied successfully", new ApplyForLeaveResponse(result.leaveApplicationId()), null));
    }

    @GetMapping("/leave")
    @Operation(summary = "List leave applications", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<LeaveApplicationResult>>> listLeaveApplications(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LeaveStatus status,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long effectiveOrgId = orgId != null ? orgId : (principal != null ? principal.activeOrganizationId() : null);
        var result = listLeaveApplicationsHandler.execute(new ListLeaveApplicationsQuery(effectiveOrgId, employeeId, status));
        return ResponseEntity.ok(new ApiResponse<>(true, "Leave applications retrieved successfully", result, null));
    }

    @PostMapping("/leave/{id}/approve")
    @PreAuthorize("hasAuthority('hr:leave:approve')")
    @Operation(summary = "Approve a leave application", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> approveLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long approverId = principal != null ? principal.userId() : 0L;
        approveLeaveHandler.execute(new ApproveLeaveCommand(id, approverId));
        return ResponseEntity.ok(new ApiResponse<>(true, "Leave application approved successfully", null, null));
    }

    @PostMapping("/leave/{id}/reject")
    @PreAuthorize("hasAuthority('hr:leave:approve')")
    @Operation(summary = "Reject a leave application", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> rejectLeave(
            @PathVariable Long id,
            @Valid @RequestBody RejectLeaveRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        rejectLeaveHandler.execute(new RejectLeaveCommand(id, request.reason()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Leave application rejected successfully", null, null));
    }

    @PostMapping("/leave/{id}/cancel")
    @Operation(summary = "Cancel a leave application", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> cancelLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        cancelLeaveHandler.execute(new CancelLeaveCommand(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Leave application cancelled successfully", null, null));
    }

    @GetMapping("/leave/balance")
    @Operation(summary = "Get leave balance for employee", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<LeaveBalanceResult>>> getLeaveBalance(
            @RequestParam Long employeeId,
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        var result = getLeaveBalanceHandler.execute(new GetLeaveBalanceQuery(employeeId, year));
        return ResponseEntity.ok(new ApiResponse<>(true, "Leave balances retrieved successfully", result, null));
    }
}
