package com.atlashub.hr.attendance.domain.entities;

import com.atlashub.hr.attendance.domain.exceptions.AlreadyClockedInException;
import com.atlashub.hr.attendance.domain.exceptions.AttendanceInvariantException;
import com.atlashub.hr.attendance.domain.exceptions.NotClockedInException;
import com.atlashub.hr.attendance.domain.valueobject.AttendanceStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttendanceRecordTest {

    @Test
    @DisplayName("Should create AttendanceRecord with valid parameters")
    void shouldCreateRecord() {
        AttendanceRecord record = AttendanceRecord.create(
                1L,
                100L,
                10L,
                LocalDate.of(2026, 7, 1),
                AttendanceStatus.PRESENT
        );

        assertThat(record.getId()).isEqualTo(1L);
        assertThat(record.getEmployeeId()).isEqualTo(100L);
        assertThat(record.getOrganizationId()).isEqualTo(10L);
        assertThat(record.getDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(record.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(record.getClockIn()).isNull();
        assertThat(record.getClockOut()).isNull();
    }

    @Test
    @DisplayName("Should mark PRESENT when clocking in on or before shift start")
    void shouldClockInOnTime() {
        AttendanceRecord record = AttendanceRecord.create(
                1L,
                100L,
                10L,
                LocalDate.of(2026, 7, 1),
                AttendanceStatus.PRESENT
        );

        ZonedDateTime shiftStart = LocalDate.of(2026, 7, 1).atTime(9, 0).atZone(ZoneId.systemDefault());
        ZonedDateTime clockInTime = LocalDate.of(2026, 7, 1).atTime(8, 55).atZone(ZoneId.systemDefault());

        record.clockIn(clockInTime, shiftStart);

        assertThat(record.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(record.getClockIn()).isEqualTo(clockInTime);
    }

    @Test
    @DisplayName("Should mark LATE when clocking in after shift start")
    void shouldClockInLate() {
        AttendanceRecord record = AttendanceRecord.create(
                1L,
                100L,
                10L,
                LocalDate.of(2026, 7, 1),
                AttendanceStatus.PRESENT
        );

        ZonedDateTime shiftStart = LocalDate.of(2026, 7, 1).atTime(9, 0).atZone(ZoneId.systemDefault());
        ZonedDateTime clockInTime = LocalDate.of(2026, 7, 1).atTime(9, 15).atZone(ZoneId.systemDefault());

        record.clockIn(clockInTime, shiftStart);

        assertThat(record.getStatus()).isEqualTo(AttendanceStatus.LATE);
        assertThat(record.getClockIn()).isEqualTo(clockInTime);
    }

    @Test
    @DisplayName("Should throw AlreadyClockedInException on duplicate clock-in")
    void shouldThrowOnDuplicateClockIn() {
        AttendanceRecord record = AttendanceRecord.create(
                1L,
                100L,
                10L,
                LocalDate.of(2026, 7, 1),
                AttendanceStatus.PRESENT
        );
        ZonedDateTime shiftStart = LocalDate.of(2026, 7, 1).atTime(9, 0).atZone(ZoneId.systemDefault());
        record.clockIn(shiftStart, shiftStart);

        assertThrows(AlreadyClockedInException.class, () -> record.clockIn(shiftStart, shiftStart));
    }

    @Test
    @DisplayName("Should calculate hours worked and overtime on clock-out")
    void shouldCalculateHoursAndOvertime() {
        AttendanceRecord record = AttendanceRecord.create(
                1L,
                100L,
                10L,
                LocalDate.of(2026, 7, 1),
                AttendanceStatus.PRESENT
        );

        ZonedDateTime clockInTime = LocalDate.of(2026, 7, 1).atTime(9, 0).atZone(ZoneId.systemDefault());
        ZonedDateTime clockOutTime = LocalDate.of(2026, 7, 1).atTime(19, 0).atZone(ZoneId.systemDefault());

        record.clockIn(clockInTime, clockInTime);
        record.clockOut(clockOutTime, 8);

        assertThat(record.getHoursWorked()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(record.getOvertimeHours()).isEqualByComparingTo(new BigDecimal("2.00"));
    }

    @Test
    @DisplayName("Should throw NotClockedInException if clock-out attempted before clock-in")
    void shouldThrowIfClockOutWithoutClockIn() {
        AttendanceRecord record = AttendanceRecord.create(
                1L,
                100L,
                10L,
                LocalDate.of(2026, 7, 1),
                AttendanceStatus.PRESENT
        );
        ZonedDateTime clockOutTime = LocalDate.of(2026, 7, 1).atTime(17, 0).atZone(ZoneId.systemDefault());

        assertThrows(NotClockedInException.class, () -> record.clockOut(clockOutTime, 8));
    }

    @Test
    @DisplayName("Should throw AttendanceInvariantException if clock-out before clock-in time")
    void shouldThrowIfClockOutBeforeClockIn() {
        AttendanceRecord record = AttendanceRecord.create(
                1L,
                100L,
                10L,
                LocalDate.of(2026, 7, 1),
                AttendanceStatus.PRESENT
        );
        ZonedDateTime clockInTime = LocalDate.of(2026, 7, 1).atTime(9, 0).atZone(ZoneId.systemDefault());
        ZonedDateTime clockOutTime = LocalDate.of(2026, 7, 1).atTime(8, 0).atZone(ZoneId.systemDefault());

        record.clockIn(clockInTime, clockInTime);

        assertThrows(AttendanceInvariantException.class, () -> record.clockOut(clockOutTime, 8));
    }

    @Test
    @DisplayName("Should mark status as ON_LEAVE")
    void shouldMarkOnLeave() {
        AttendanceRecord record = AttendanceRecord.create(
                1L,
                100L,
                10L,
                LocalDate.of(2026, 7, 1),
                AttendanceStatus.PRESENT
        );

        record.markOnLeave();

        assertThat(record.getStatus()).isEqualTo(AttendanceStatus.ON_LEAVE);
    }
}
