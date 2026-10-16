package com.atlashub.hr.attendance.application.command.ClockIn;

import com.atlashub.hr.attendance.domain.entities.AttendanceRecord;
import com.atlashub.hr.attendance.domain.exceptions.AlreadyClockedInException;
import com.atlashub.hr.attendance.domain.repositories.AttendanceRecordRepository;
import com.atlashub.hr.attendance.domain.valueobject.AttendanceStatus;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Component
public class ClockInHandler extends Command<ClockInCommand, Void> {

    private final AttendanceRecordRepository attendanceRecordRepository;

    public ClockInHandler(AttendanceRecordRepository attendanceRecordRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    @Override
    @Transactional
    public Void execute(ClockInCommand command) {
        LocalDate today = LocalDate.now();
        Optional<AttendanceRecord> existingOpt = attendanceRecordRepository.findByEmployeeIdAndDate(command.employeeId(), today);

        if (existingOpt.isPresent() && existingOpt.get().getClockIn() != null) {
            throw new AlreadyClockedInException();
        }

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime shiftStart = today.atTime(9, 0).atZone(ZoneId.systemDefault());

        AttendanceRecord record = existingOpt.orElseGet(() ->
                AttendanceRecord.create(null, command.employeeId(), command.organizationId(), today, AttendanceStatus.PRESENT)
        );

        record.clockIn(now, shiftStart);
        attendanceRecordRepository.save(record);

        return null;
    }
}
