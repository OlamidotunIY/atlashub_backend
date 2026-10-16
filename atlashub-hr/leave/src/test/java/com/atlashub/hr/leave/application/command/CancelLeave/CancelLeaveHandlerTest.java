package com.atlashub.hr.leave.application.command.CancelLeave;

import com.atlashub.hr.leave.domain.entities.LeaveApplication;
import com.atlashub.hr.leave.domain.entities.LeaveBalance;
import com.atlashub.hr.leave.domain.exceptions.LeaveApplicationNotFoundException;
import com.atlashub.hr.leave.domain.repositories.LeaveApplicationRepository;
import com.atlashub.hr.leave.domain.repositories.LeaveBalanceRepository;
import com.atlashub.hr.leave.domain.valueobject.LeaveStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CancelLeaveHandlerTest {

    private LeaveApplicationRepository leaveApplicationRepository;
    private LeaveBalanceRepository leaveBalanceRepository;
    private CancelLeaveHandler handler;

    @BeforeEach
    void setUp() {
        leaveApplicationRepository = Mockito.mock(LeaveApplicationRepository.class);
        leaveBalanceRepository = Mockito.mock(LeaveBalanceRepository.class);
        handler = new CancelLeaveHandler(leaveApplicationRepository, leaveBalanceRepository);
    }

    @Test
    @DisplayName("Should cancel pending leave without touching balance")
    void shouldCancelPendingLeave() {
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
        when(leaveApplicationRepository.findById(10L)).thenReturn(Optional.of(app));

        handler.execute(new CancelLeaveCommand(10L));

        assertThat(app.getStatus()).isEqualTo(LeaveStatus.CANCELLED);
        verify(leaveApplicationRepository).save(app);
    }

    @Test
    @DisplayName("Should cancel approved leave and restore balance")
    void shouldCancelApprovedLeaveAndRestoreBalance() {
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
        app.approve(999L);
        LeaveBalance balance = new LeaveBalance(1L, 100L, 5L, 2026, 20, 5, 15);

        when(leaveApplicationRepository.findById(10L)).thenReturn(Optional.of(app));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYearWithLock(100L, 5L, 2026))
                .thenReturn(Optional.of(balance));

        handler.execute(new CancelLeaveCommand(10L));

        assertThat(app.getStatus()).isEqualTo(LeaveStatus.CANCELLED);
        assertThat(balance.getUsed()).isZero();
        assertThat(balance.getRemaining()).isEqualTo(20);
        verify(leaveBalanceRepository).save(balance);
        verify(leaveApplicationRepository).save(app);
    }

    @Test
    @DisplayName("Should throw when application is not found")
    void shouldThrowWhenNotFound() {
        when(leaveApplicationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(LeaveApplicationNotFoundException.class, () ->
                handler.execute(new CancelLeaveCommand(999L))
        );
    }
}
