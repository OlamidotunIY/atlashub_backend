package com.atlashub.hr.attendance.application.command.MarkAttendanceAsLeave;

import com.atlashub.hr.attendance.domain.entities.AttendanceRecord;
import com.atlashub.hr.attendance.domain.repositories.AttendanceRecordRepository;
import com.atlashub.hr.attendance.domain.valueobject.AttendanceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MarkAttendanceAsLeaveHandlerTest {

    private AttendanceRecordRepository repository;
    private MarkAttendanceAsLeaveHandler handler;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(AttendanceRecordRepository.class);
        handler = new MarkAttendanceAsLeaveHandler(repository);
    }

    @Test
    @DisplayName("Should mark date range as ON_LEAVE")
    void shouldMarkDateRangeAsLeave() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 3); // 3 days

        when(repository.findByEmployeeIdAndDate(any(), any())).thenReturn(Optional.empty());

        handler.execute(new MarkAttendanceAsLeaveCommand(100L, 10L, start, end));

        verify(repository, times(3)).save(any(AttendanceRecord.class));
    }
}
