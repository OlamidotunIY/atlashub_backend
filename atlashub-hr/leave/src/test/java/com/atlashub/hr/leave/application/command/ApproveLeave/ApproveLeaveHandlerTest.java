package com.atlashub.hr.leave.application.command.ApproveLeave;

import com.atlashub.hr.leave.domain.entities.LeaveApplication;
import com.atlashub.hr.leave.domain.entities.LeaveBalance;
import com.atlashub.hr.leave.domain.entities.LeaveType;
import com.atlashub.hr.leave.domain.exceptions.LeaveApplicationNotFoundException;
import com.atlashub.hr.leave.domain.repositories.LeaveApplicationRepository;
import com.atlashub.hr.leave.domain.repositories.LeaveBalanceRepository;
import com.atlashub.hr.leave.domain.repositories.LeaveTypeRepository;
import com.atlashub.hr.leave.domain.valueobject.LeaveStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApproveLeaveHandlerTest {

    private LeaveApplicationRepository leaveApplicationRepository;
    private LeaveBalanceRepository leaveBalanceRepository;
    private LeaveTypeRepository leaveTypeRepository;
    private ApproveLeaveHandler handler;

    @BeforeEach
    void setUp() {
        leaveApplicationRepository = Mockito.mock(LeaveApplicationRepository.class);
        leaveBalanceRepository = Mockito.mock(LeaveBalanceRepository.class);
        leaveTypeRepository = Mockito.mock(LeaveTypeRepository.class);
        handler = new ApproveLeaveHandler(leaveApplicationRepository, leaveBalanceRepository, leaveTypeRepository);
    }

    @Test
    @DisplayName("Should approve leave and deduct from existing balance")
    void shouldApproveLeaveWithExistingBalance() {
        LeaveApplication app = LeaveApplication.create(
                10L,
                100L,
                10L,
                5L,
                LocalDate.of(2026, 8, 3),
                LocalDate.of(2026, 8, 7),
                5,
                "Summer break"
        );
        LeaveBalance balance = LeaveBalance.create(1L, 100L, 5L, 2026, 20);

        when(leaveApplicationRepository.findById(10L)).thenReturn(Optional.of(app));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYearWithLock(100L, 5L, 2026))
                .thenReturn(Optional.of(balance));

        handler.execute(new ApproveLeaveCommand(10L, 999L));

        assertThat(app.getStatus()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(balance.getUsed()).isEqualTo(5);
        assertThat(balance.getRemaining()).isEqualTo(15);
        verify(leaveBalanceRepository).save(balance);
        verify(leaveApplicationRepository).save(app);
    }

    @Test
    @DisplayName("Should initialize balance from leave type when balance is absent and approve")
    void shouldInitializeBalanceAndApprove() {
        LeaveApplication app = LeaveApplication.create(
                10L,
                100L,
                10L,
                5L,
                LocalDate.of(2026, 8, 3),
                LocalDate.of(2026, 8, 7),
                5,
                "Summer break"
        );
        LeaveType type = LeaveType.create(5L, 10L, "Annual Leave", 25, true, true, false, 0);

        when(leaveApplicationRepository.findById(10L)).thenReturn(Optional.of(app));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYearWithLock(100L, 5L, 2026))
                .thenReturn(Optional.empty());
        when(leaveTypeRepository.findById(5L)).thenReturn(Optional.of(type));

        handler.execute(new ApproveLeaveCommand(10L, 999L));

        assertThat(app.getStatus()).isEqualTo(LeaveStatus.APPROVED);
        verify(leaveBalanceRepository).save(any(LeaveBalance.class));
        verify(leaveApplicationRepository).save(app);
    }

    @Test
    @DisplayName("Should throw LeaveApplicationNotFoundException when application does not exist")
    void shouldThrowWhenApplicationNotFound() {
        when(leaveApplicationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(LeaveApplicationNotFoundException.class, () ->
                handler.execute(new ApproveLeaveCommand(999L, 1L))
        );
    }
}
