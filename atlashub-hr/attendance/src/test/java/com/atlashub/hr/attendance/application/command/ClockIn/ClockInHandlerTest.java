package com.atlashub.hr.attendance.application.command.ClockIn;

import com.atlashub.hr.attendance.domain.entities.AttendanceRecord;
import com.atlashub.hr.attendance.domain.exceptions.AlreadyClockedInException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClockInHandlerTest {

    private AttendanceRecordRepository repository;
    private ClockInHandler handler;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(AttendanceRecordRepository.class);
        handler = new ClockInHandler(repository);
    }

    @Test
    @DisplayName("Should create new record and clock in when absent")
    void shouldClockInWhenNoExistingRecord() {
        when(repository.findByEmployeeIdAndDate(100L, LocalDate.now())).thenReturn(Optional.empty());

        handler.execute(new ClockInCommand(100L, 10L));

        verify(repository).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Should throw AlreadyClockedInException if employee already clocked in today")
    void shouldThrowIfAlreadyClockedIn() {
        AttendanceRecord existing = AttendanceRecord.create(1L, 100L, 10L, LocalDate.now(), AttendanceStatus.PRESENT);
        existing.clockIn(ZonedDateTime.now(), ZonedDateTime.now());

        when(repository.findByEmployeeIdAndDate(100L, LocalDate.now())).thenReturn(Optional.of(existing));

        assertThrows(AlreadyClockedInException.class, () ->
                handler.execute(new ClockInCommand(100L, 10L))
        );
    }
}
