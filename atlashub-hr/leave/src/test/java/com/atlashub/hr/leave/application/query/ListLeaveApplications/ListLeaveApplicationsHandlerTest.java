package com.atlashub.hr.leave.application.query.ListLeaveApplications;

import com.atlashub.hr.leave.domain.entities.LeaveApplication;
import com.atlashub.hr.leave.domain.entities.LeaveType;
import com.atlashub.hr.leave.domain.repositories.LeaveApplicationRepository;
import com.atlashub.hr.leave.domain.repositories.LeaveTypeRepository;
import com.atlashub.hr.leave.domain.valueobject.LeaveStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ListLeaveApplicationsHandlerTest {

    private LeaveApplicationRepository leaveApplicationRepository;
    private LeaveTypeRepository leaveTypeRepository;
    private ListLeaveApplicationsHandler handler;

    @BeforeEach
    void setUp() {
        leaveApplicationRepository = Mockito.mock(LeaveApplicationRepository.class);
        leaveTypeRepository = Mockito.mock(LeaveTypeRepository.class);
        handler = new ListLeaveApplicationsHandler(leaveApplicationRepository, leaveTypeRepository);
    }

    @Test
    @DisplayName("Should list leave applications by organization and status")
    void shouldListApplications() {
        LeaveApplication app = LeaveApplication.create(
                1L,
                100L,
                10L,
                5L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 5),
                5,
                "Vacation"
        );
        LeaveType type = LeaveType.create(5L, 10L, "Annual Leave", 20, true, true, false, 0);

        when(leaveApplicationRepository.findByOrganizationIdAndStatus(10L, LeaveStatus.PENDING))
                .thenReturn(List.of(app));
        when(leaveTypeRepository.findById(5L)).thenReturn(Optional.of(type));

        List<LeaveApplicationResult> results = handler.execute(
                new ListLeaveApplicationsQuery(10L, null, LeaveStatus.PENDING)
        );

        assertThat(results).hasSize(1);
        LeaveApplicationResult result = results.get(0);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.employeeId()).isEqualTo(100L);
        assertThat(result.leaveTypeName()).isEqualTo("Annual Leave");
        assertThat(result.status()).isEqualTo(LeaveStatus.PENDING);
    }
}
