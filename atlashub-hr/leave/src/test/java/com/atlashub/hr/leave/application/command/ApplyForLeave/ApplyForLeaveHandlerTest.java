package com.atlashub.hr.leave.application.command.ApplyForLeave;

import com.atlashub.hr.leave.application.command.ApproveLeave.ApproveLeaveCommand;
import com.atlashub.hr.leave.application.command.ApproveLeave.ApproveLeaveHandler;
import com.atlashub.hr.leave.domain.entities.LeaveApplication;
import com.atlashub.hr.leave.domain.entities.LeaveType;
import com.atlashub.hr.leave.domain.exceptions.LeaveDatesConflictException;
import com.atlashub.hr.leave.domain.exceptions.LeaveTypeNotFoundException;
import com.atlashub.hr.leave.domain.repositories.LeaveApplicationRepository;
import com.atlashub.hr.leave.domain.repositories.LeaveTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplyForLeaveHandlerTest {

    private LeaveTypeRepository leaveTypeRepository;
    private LeaveApplicationRepository leaveApplicationRepository;
    private ApproveLeaveHandler approveLeaveHandler;
    private ApplyForLeaveHandler handler;

    @BeforeEach
    void setUp() {
        leaveTypeRepository = Mockito.mock(LeaveTypeRepository.class);
        leaveApplicationRepository = Mockito.mock(LeaveApplicationRepository.class);
        approveLeaveHandler = Mockito.mock(ApproveLeaveHandler.class);
        handler = new ApplyForLeaveHandler(leaveTypeRepository, leaveApplicationRepository, approveLeaveHandler);
    }

    @Test
    @DisplayName("Should apply for leave requiring approval")
    void shouldApplyForLeaveRequiringApproval() {
        LeaveType type = LeaveType.create(5L, 10L, "Annual Leave", 20, true, true, false, 0);
        when(leaveTypeRepository.findById(5L)).thenReturn(Optional.of(type));
        when(leaveApplicationRepository.existsByEmployeeIdAndStatusInAndDateRange(any(), any(), any(), any()))
                .thenReturn(false);
        when(leaveApplicationRepository.save(any(LeaveApplication.class))).thenAnswer(invocation -> {
            LeaveApplication app = invocation.getArgument(0);
            return new LeaveApplication(100L, app.getEmployeeId(), app.getOrganizationId(), app.getLeaveTypeId(),
                    app.getStartDate(), app.getEndDate(), app.getDaysRequested(), app.getReason(),
                    app.getStatus(), app.getApprovedBy(), app.getApprovedAt());
        });

        ApplyForLeaveCommand command = new ApplyForLeaveCommand(
                1L,
                10L,
                5L,
                LocalDate.of(2026, 7, 6), // Monday
                LocalDate.of(2026, 7, 10), // Friday
                "Vacation"
        );

        ApplyForLeaveResponse response = handler.execute(command);

        assertThat(response.leaveApplicationId()).isEqualTo(100L);
        verify(leaveApplicationRepository).save(any(LeaveApplication.class));
        verify(approveLeaveHandler, never()).execute(any());
    }

    @Test
    @DisplayName("Should auto-approve when leave type requiresApproval is false")
    void shouldAutoApproveWhenRequiresApprovalIsFalse() {
        LeaveType type = LeaveType.create(5L, 10L, "Casual Leave", 5, true, false, false, 0);
        when(leaveTypeRepository.findById(5L)).thenReturn(Optional.of(type));
        when(leaveApplicationRepository.existsByEmployeeIdAndStatusInAndDateRange(any(), any(), any(), any()))
                .thenReturn(false);
        when(leaveApplicationRepository.save(any(LeaveApplication.class))).thenAnswer(invocation -> {
            LeaveApplication app = invocation.getArgument(0);
            return new LeaveApplication(101L, app.getEmployeeId(), app.getOrganizationId(), app.getLeaveTypeId(),
                    app.getStartDate(), app.getEndDate(), app.getDaysRequested(), app.getReason(),
                    app.getStatus(), app.getApprovedBy(), app.getApprovedAt());
        });

        ApplyForLeaveCommand command = new ApplyForLeaveCommand(
                1L,
                10L,
                5L,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 7),
                "Casual day off"
        );

        ApplyForLeaveResponse response = handler.execute(command);

        assertThat(response.leaveApplicationId()).isEqualTo(101L);
        verify(approveLeaveHandler).execute(any(ApproveLeaveCommand.class));
    }

    @Test
    @DisplayName("Should throw LeaveDatesConflictException if dates overlap")
    void shouldThrowWhenDatesConflict() {
        LeaveType type = LeaveType.create(5L, 10L, "Annual Leave", 20, true, true, false, 0);
        when(leaveTypeRepository.findById(5L)).thenReturn(Optional.of(type));
        when(leaveApplicationRepository.existsByEmployeeIdAndStatusInAndDateRange(any(), any(), any(), any()))
                .thenReturn(true);

        ApplyForLeaveCommand command = new ApplyForLeaveCommand(
                1L,
                10L,
                5L,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 10),
                "Vacation"
        );

        assertThrows(LeaveDatesConflictException.class, () -> handler.execute(command));
    }

    @Test
    @DisplayName("Should throw LeaveTypeNotFoundException if leave type does not exist")
    void shouldThrowWhenLeaveTypeNotFound() {
        when(leaveTypeRepository.findById(99L)).thenReturn(Optional.empty());

        ApplyForLeaveCommand command = new ApplyForLeaveCommand(
                1L,
                10L,
                99L,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 10),
                "Vacation"
        );

        assertThrows(LeaveTypeNotFoundException.class, () -> handler.execute(command));
    }
}
