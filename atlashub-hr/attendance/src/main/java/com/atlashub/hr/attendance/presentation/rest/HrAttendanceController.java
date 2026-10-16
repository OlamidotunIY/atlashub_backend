package com.atlashub.hr.attendance.presentation.rest;

import com.atlashub.hr.attendance.application.command.ClockIn.ClockInCommand;
import com.atlashub.hr.attendance.application.command.ClockIn.ClockInHandler;
import com.atlashub.hr.attendance.application.command.ClockOut.ClockOutCommand;
import com.atlashub.hr.attendance.application.command.ClockOut.ClockOutHandler;
import com.atlashub.hr.attendance.application.query.GetAttendanceSummary.AttendanceSummaryResult;
import com.atlashub.hr.attendance.application.query.GetAttendanceSummary.GetAttendanceSummaryHandler;
import com.atlashub.hr.attendance.application.query.GetAttendanceSummary.GetAttendanceSummaryQuery;
import com.atlashub.hr.attendance.presentation.dto.ClockInRequest;
import com.atlashub.hr.attendance.presentation.dto.ClockOutRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "HR Attendance", description = "Endpoints for employee clock-in, clock-out, and attendance summaries")
public class HrAttendanceController {

    private final ClockInHandler clockInHandler;
    private final ClockOutHandler clockOutHandler;
    private final GetAttendanceSummaryHandler getAttendanceSummaryHandler;

    public HrAttendanceController(
            ClockInHandler clockInHandler,
            ClockOutHandler clockOutHandler,
            GetAttendanceSummaryHandler getAttendanceSummaryHandler
    ) {
        this.clockInHandler = clockInHandler;
        this.clockOutHandler = clockOutHandler;
        this.getAttendanceSummaryHandler = getAttendanceSummaryHandler;
    }

    @PostMapping("/attendance/clock-in")
    @Operation(summary = "Clock in for today", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> clockIn(
            @Valid @RequestBody ClockInRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long organizationId = request.organizationId() != null
                ? request.organizationId()
                : (principal != null ? principal.activeOrganizationId() : null);

        clockInHandler.execute(new ClockInCommand(request.employeeId(), organizationId));
        return ResponseEntity.ok(new ApiResponse<>(true, "Clocked in successfully", null, null));
    }

    @PostMapping("/attendance/clock-out")
    @Operation(summary = "Clock out for today", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> clockOut(
            @Valid @RequestBody ClockOutRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long organizationId = request.organizationId() != null
                ? request.organizationId()
                : (principal != null ? principal.activeOrganizationId() : null);

        clockOutHandler.execute(new ClockOutCommand(request.employeeId(), organizationId));
        return ResponseEntity.ok(new ApiResponse<>(true, "Clocked out successfully", null, null));
    }

    @GetMapping("/attendance/summary")
    @Operation(summary = "Get monthly attendance summary for employee", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<AttendanceSummaryResult>> getAttendanceSummary(
            @RequestParam(required = false) Long orgId,
            @RequestParam Long employeeId,
            @RequestParam int month,
            @RequestParam int year,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Long effectiveOrgId = orgId != null
                ? orgId
                : (principal != null ? principal.activeOrganizationId() : null);

        var result = getAttendanceSummaryHandler.execute(
                new GetAttendanceSummaryQuery(effectiveOrgId, employeeId, month, year)
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Attendance summary retrieved successfully", result, null));
    }
}
