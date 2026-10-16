package com.atlashub.hr.leave.domain.entities;

import com.atlashub.hr.leave.domain.events.LeaveApprovedEvent;
import com.atlashub.hr.leave.domain.events.LeaveRejectedEvent;
import com.atlashub.hr.leave.domain.exceptions.InvalidLeaveStateException;
import com.atlashub.hr.leave.domain.valueobject.LeaveStatus;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LeaveApplicationTest {

    private LeaveApplication createSampleApplication() {
        return LeaveApplication.create(
                1L,
                100L,
                10L,
                5L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 5),
                5,
                "Vacation"
        );
    }

    @Test
    @DisplayName("Should create LeaveApplication with PENDING status")
    void shouldCreateApplicationSuccessfully() {
        LeaveApplication app = createSampleApplication();

        assertThat(app.getId()).isEqualTo(1L);
        assertThat(app.getEmployeeId()).isEqualTo(100L);
        assertThat(app.getOrganizationId()).isEqualTo(10L);
        assertThat(app.getLeaveTypeId()).isEqualTo(5L);
        assertThat(app.getStatus()).isEqualTo(LeaveStatus.PENDING);
        assertThat(app.getDaysRequested()).isEqualTo(5);
        assertThat(app.getReason()).isEqualTo("Vacation");
    }

    @Test
    @DisplayName("Should approve PENDING application and register LeaveApprovedEvent")
    void shouldApproveApplication() {
        LeaveApplication app = createSampleApplication();

        app.approve(999L);

        assertThat(app.getStatus()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(app.getApprovedBy()).isEqualTo(999L);
        assertThat(app.getApprovedAt()).isNotNull();
        assertThat(app.peekDomainEvents()).hasSize(1);
        assertThat(app.peekDomainEvents().get(0)).isInstanceOf(LeaveApprovedEvent.class);
    }

    @Test
    @DisplayName("Should reject PENDING application and register LeaveRejectedEvent")
    void shouldRejectApplication() {
        LeaveApplication app = createSampleApplication();

        app.reject("Understaffed");

        assertThat(app.getStatus()).isEqualTo(LeaveStatus.REJECTED);
        assertThat(app.peekDomainEvents()).hasSize(1);
        assertThat(app.peekDomainEvents().get(0)).isInstanceOf(LeaveRejectedEvent.class);
    }

    @Test
    @DisplayName("Should cancel application from PENDING or APPROVED state")
    void shouldCancelApplication() {
        LeaveApplication app = createSampleApplication();
        app.cancel();
        assertThat(app.getStatus()).isEqualTo(LeaveStatus.CANCELLED);

        LeaveApplication app2 = createSampleApplication();
        app2.approve(999L);
        app2.cancel();
        assertThat(app2.getStatus()).isEqualTo(LeaveStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should throw when approving already approved or rejected application")
    void shouldThrowOnInvalidStateTransitions() {
        LeaveApplication app = createSampleApplication();
        app.approve(999L);

        assertThrows(InvalidLeaveStateException.class, () -> app.approve(999L));
        assertThrows(InvalidLeaveStateException.class, () -> app.reject("Too late"));
    }

    @Test
    @DisplayName("Should reject invalid date ranges")
    void shouldRejectInvalidDates() {
        assertThrows(BusinessRuleException.class, () ->
                LeaveApplication.create(
                        1L,
                        100L,
                        10L,
                        5L,
                        LocalDate.of(2026, 6, 10),
                        LocalDate.of(2026, 6, 5),
                        5,
                        "Invalid dates"
                )
        );
    }
}
