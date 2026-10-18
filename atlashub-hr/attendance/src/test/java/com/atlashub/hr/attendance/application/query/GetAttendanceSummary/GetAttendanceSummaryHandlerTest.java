package com.atlashub.hr.attendance.application.query.GetAttendanceSummary;

import com.atlashub.hr.attendance.domain.entities.AttendanceRecord;
import com.atlashub.hr.attendance.domain.repositories.AttendanceRecordRepository;
import com.atlashub.hr.attendance.domain.valueobject.AttendanceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class GetAttendanceSummaryHandlerTest {

    private AttendanceRecordRepository repository;
    private GetAttendanceSummaryHandler handler;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(AttendanceRecordRepository.class);
        handler = new GetAttendanceSummaryHandler(repository);
    }

    @Test
    @DisplayName("Should calculate monthly attendance summary accurately")
    void shouldCalculateSummary() {
        LocalDate date1 = LocalDate.of(2026, 7, 1);
        LocalDate date2 = LocalDate.of(2026, 7, 2);

        AttendanceRecord rec1 = AttendanceRecord.create(1L, 100L, 10L, date1, AttendanceStatus.PRESENT);
        rec1.clockIn(date1.atTime(8, 55).atZone(ZoneId.systemDefault()), date1.atTime(9, 0).atZone(ZoneId.systemDefault()));
        rec1.clockOut(date1.atTime(17, 0).atZone(ZoneId.systemDefault()), 8);

        AttendanceRecord rec2 = AttendanceRecord.create(2L, 100L, 10L, date2, AttendanceStatus.PRESENT);
        rec2.clockIn(date2.atTime(9, 30).atZone(ZoneId.systemDefault()), date2.atTime(9, 0).atZone(ZoneId.systemDefault()));
        rec2.clockOut(date2.atTime(19, 30).atZone(ZoneId.systemDefault()), 8);

        when(repository.findByOrganizationIdAndEmployeeIdAndDateBetween(eq(10L), eq(100L), any(), any()))
                .thenReturn(List.of(rec1, rec2));

        AttendanceSummaryResult result = handler.execute(new GetAttendanceSummaryQuery(10L, 100L, 7, 2026));

        assertThat(result.totalDays()).isEqualTo(31);
        assertThat(result.presentDays()).isEqualTo(2);
        assertThat(result.lateDays()).isEqualTo(1);
        assertThat(result.totalHoursWorked()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.totalOvertimeHours()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.records()).hasSize(2);
    }
}
