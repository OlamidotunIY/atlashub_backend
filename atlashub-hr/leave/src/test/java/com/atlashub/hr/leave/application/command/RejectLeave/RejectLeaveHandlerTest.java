package com.atlashub.hr.leave.application.command.RejectLeave;

import com.atlashub.hr.leave.domain.entities.LeaveApplication;
import com.atlashub.hr.leave.domain.exceptions.LeaveApplicationNotFoundException;
import com.atlashub.hr.leave.domain.repositories.LeaveApplicationRepository;
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

class RejectLeaveHandlerTest {

    private LeaveApplicationRepository leaveApplicationRepository;
    private RejectLeaveHandler handler;

    @BeforeEach
    void setUp() {
        leaveApplicationRepository = Mockito.mock(LeaveApplicationRepository.class);
        handler = new RejectLeaveHandler(leaveApplicationRepository);
    }

    @Test
    @DisplayName("Should reject pending leave application")
    void shouldRejectLeaveApplication() {
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

        handler.execute(new RejectLeaveCommand(10L, "Conflict with product launch"));

        assertThat(app.getStatus()).isEqualTo(LeaveStatus.REJECTED);
        verify(leaveApplicationRepository).save(app);
    }

    @Test
    @DisplayName("Should throw when application is not found")
    void shouldThrowWhenNotFound() {
        when(leaveApplicationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(LeaveApplicationNotFoundException.class, () ->
                handler.execute(new RejectLeaveCommand(10L, "Reason"))
        );
    }
}
