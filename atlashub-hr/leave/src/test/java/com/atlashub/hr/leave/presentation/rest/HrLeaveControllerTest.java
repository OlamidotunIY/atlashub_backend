package com.atlashub.hr.leave.presentation.rest;

import com.atlashub.hr.leave.application.command.ApplyForLeave.ApplyForLeaveCommand;
import com.atlashub.hr.leave.application.command.ApplyForLeave.ApplyForLeaveHandler;
import com.atlashub.hr.leave.application.command.ApplyForLeave.ApplyForLeaveResponse;
import com.atlashub.hr.leave.application.command.ApproveLeave.ApproveLeaveCommand;
import com.atlashub.hr.leave.application.command.ApproveLeave.ApproveLeaveHandler;
import com.atlashub.hr.leave.application.command.CancelLeave.CancelLeaveCommand;
import com.atlashub.hr.leave.application.command.CancelLeave.CancelLeaveHandler;
import com.atlashub.hr.leave.application.command.CreateLeaveType.CreateLeaveTypeCommand;
import com.atlashub.hr.leave.application.command.CreateLeaveType.CreateLeaveTypeHandler;
import com.atlashub.hr.leave.application.command.CreateLeaveType.CreateLeaveTypeResponse;
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
import com.atlashub.hr.leave.presentation.dto.CreateLeaveTypeRequest;
import com.atlashub.hr.leave.presentation.dto.RejectLeaveRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HrLeaveControllerTest {

    private CreateLeaveTypeHandler createLeaveTypeHandler;
    private ApplyForLeaveHandler applyForLeaveHandler;
    private ApproveLeaveHandler approveLeaveHandler;
    private RejectLeaveHandler rejectLeaveHandler;
    private CancelLeaveHandler cancelLeaveHandler;
    private GetLeaveBalanceHandler getLeaveBalanceHandler;
    private ListLeaveApplicationsHandler listLeaveApplicationsHandler;
    private HrLeaveController controller;
    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        createLeaveTypeHandler = Mockito.mock(CreateLeaveTypeHandler.class);
        applyForLeaveHandler = Mockito.mock(ApplyForLeaveHandler.class);
        approveLeaveHandler = Mockito.mock(ApproveLeaveHandler.class);
        rejectLeaveHandler = Mockito.mock(RejectLeaveHandler.class);
        cancelLeaveHandler = Mockito.mock(CancelLeaveHandler.class);
        getLeaveBalanceHandler = Mockito.mock(GetLeaveBalanceHandler.class);
        listLeaveApplicationsHandler = Mockito.mock(ListLeaveApplicationsHandler.class);

        controller = new HrLeaveController(
                createLeaveTypeHandler,
                applyForLeaveHandler,
                approveLeaveHandler,
                rejectLeaveHandler,
                cancelLeaveHandler,
                getLeaveBalanceHandler,
                listLeaveApplicationsHandler
        );

        principal = new AuthenticatedPrincipal(
                100L,
                10L,
                "TEST",
                "session-1",
                "token-1",
                ZonedDateTime.now().plusHours(1)
        );
    }

    @Test
    @DisplayName("POST /leave-types should create leave type")
    void shouldCreateLeaveType() {
        CreateLeaveTypeRequest request = new CreateLeaveTypeRequest(
                10L,
                "Annual Leave",
                20,
                true,
                true,
                false,
                0
        );

        when(createLeaveTypeHandler.execute(any(CreateLeaveTypeCommand.class)))
                .thenReturn(new CreateLeaveTypeResponse(50L));

        ResponseEntity<ApiResponse<com.atlashub.hr.leave.presentation.dto.CreateLeaveTypeResponse>> response =
                controller.createLeaveType(request, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data().leaveTypeId()).isEqualTo(50L);
    }

    @Test
    @DisplayName("POST /leave/apply should apply for leave")
    void shouldApplyForLeave() {
        ApplyForLeaveRequest request = new ApplyForLeaveRequest(
                1L,
                5L,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 10),
                "Vacation"
        );

        when(applyForLeaveHandler.execute(any(ApplyForLeaveCommand.class)))
                .thenReturn(new ApplyForLeaveResponse(100L));

        ResponseEntity<ApiResponse<com.atlashub.hr.leave.presentation.dto.ApplyForLeaveResponse>> response =
                controller.applyForLeave(request, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data().leaveApplicationId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("POST /leave/{id}/approve should approve leave")
    void shouldApproveLeave() {
        ResponseEntity<ApiResponse<Void>> response = controller.approveLeave(100L, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(approveLeaveHandler).execute(new ApproveLeaveCommand(100L, 100L));
    }

    @Test
    @DisplayName("POST /leave/{id}/reject should reject leave")
    void shouldRejectLeave() {
        RejectLeaveRequest request = new RejectLeaveRequest("Not approved");
        ResponseEntity<ApiResponse<Void>> response = controller.rejectLeave(100L, request, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(rejectLeaveHandler).execute(new RejectLeaveCommand(100L, "Not approved"));
    }

    @Test
    @DisplayName("POST /leave/{id}/cancel should cancel leave")
    void shouldCancelLeave() {
        ResponseEntity<ApiResponse<Void>> response = controller.cancelLeave(100L, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(cancelLeaveHandler).execute(new CancelLeaveCommand(100L));
    }

    @Test
    @DisplayName("GET /leave/balance should return balances")
    void shouldGetLeaveBalance() {
        LeaveBalanceResult result = new LeaveBalanceResult(5L, "Annual Leave", 2026, 20, 5, 15);
        when(getLeaveBalanceHandler.execute(any(GetLeaveBalanceQuery.class))).thenReturn(List.of(result));

        ResponseEntity<ApiResponse<List<LeaveBalanceResult>>> response =
                controller.getLeaveBalance(1L, 2026, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).hasSize(1);
    }

    @Test
    @DisplayName("GET /leave should list applications")
    void shouldListApplications() {
        LeaveApplicationResult result = new LeaveApplicationResult(
                100L,
                1L,
                "Employee #1",
                "Annual Leave",
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 10),
                5,
                "Vacation",
                LeaveStatus.PENDING,
                null,
                null
        );
        when(listLeaveApplicationsHandler.execute(any(ListLeaveApplicationsQuery.class)))
                .thenReturn(List.of(result));

        ResponseEntity<ApiResponse<List<LeaveApplicationResult>>> response =
                controller.listLeaveApplications(10L, 1L, LeaveStatus.PENDING, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).hasSize(1);
    }
}
