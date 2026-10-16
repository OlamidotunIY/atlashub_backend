package com.atlashub.hr.attendance.application.command.ClockOut;

import com.atlashub.hr.attendance.domain.entities.AttendanceRecord;
import com.atlashub.hr.attendance.domain.exceptions.NotClockedInException;
import com.atlashub.hr.attendance.domain.repositories.AttendanceRecordRepository;
import com.atlashub.hr.attendance.domain.valueobject.AttendanceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClockOutHandlerTest {

    private AttendanceRecordRepository repository;
    private ClockOutHandler handler;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(AttendanceRecordRepository.class);
        handler = new ClockOutHandler(repository);
    }

    @Test
    @DisplayName("Should clock out successfully when clocked in")
    void shouldClockOutSuccessfully() {
        AttendanceRecord record = AttendanceRecord.create(1L, 100L, 10L, LocalDate.now(), AttendanceStatus.PRESENT);
        record.clockIn(ZonedDateTime.now().minusHours(8), ZonedDateTime.now().minusHours(8));

        when(repository.findByEmployeeIdAndDate(100L, LocalDate.now())).thenReturn(Optional.of(record));

        handler.execute(new ClockOutCommand(100L, 10L));

        assertThat(record.getClockOut()).isNotNull();
        assertThat(record.getHoursWorked()).isNotNull();
        verify(repository).save(record);
    }

    @Test
    @DisplayName("Should throw NotClockedInException when no record exists for today")
    void shouldThrowWhenNoRecordForToday() {
        when(repository.findByEmployeeIdAndDate(100L, LocalDate.now())).thenReturn(Optional.empty());

        assertThrows(NotClockedInException.class, () ->
                handler.execute(new ClockOutCommand(100L, 10L))
        );
    }
}
