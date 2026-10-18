package com.atlashub.hr.attendance.application.command.ClockOut;

import com.atlashub.hr.attendance.domain.entities.AttendanceRecord;
import com.atlashub.hr.attendance.domain.exceptions.NotClockedInException;
import com.atlashub.hr.attendance.domain.repositories.AttendanceRecordRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Component
public class ClockOutHandler extends Command<ClockOutCommand, Void> {

    private final AttendanceRecordRepository attendanceRecordRepository;

    public ClockOutHandler(AttendanceRecordRepository attendanceRecordRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    @Override
    @Transactional
    public Void execute(ClockOutCommand command) {
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRecordRepository.findByEmployeeIdAndDate(command.employeeId(), today)
                .orElseThrow(NotClockedInException::new);

        int configuredShiftHours = 8;
        record.clockOut(ZonedDateTime.now(), configuredShiftHours);
        attendanceRecordRepository.save(record);

        return null;
    }
}
