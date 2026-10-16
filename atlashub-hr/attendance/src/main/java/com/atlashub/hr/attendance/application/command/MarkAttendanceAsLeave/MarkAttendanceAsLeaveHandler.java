package com.atlashub.hr.attendance.application.command.MarkAttendanceAsLeave;

import com.atlashub.hr.attendance.domain.entities.AttendanceRecord;
import com.atlashub.hr.attendance.domain.repositories.AttendanceRecordRepository;
import com.atlashub.hr.attendance.domain.valueobject.AttendanceStatus;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class MarkAttendanceAsLeaveHandler extends Command<MarkAttendanceAsLeaveCommand, Void> {

    private final AttendanceRecordRepository attendanceRecordRepository;

    public MarkAttendanceAsLeaveHandler(AttendanceRecordRepository attendanceRecordRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    @Override
    @Transactional
    public Void execute(MarkAttendanceAsLeaveCommand command) {
        LocalDate current = command.startDate();
        while (!current.isAfter(command.endDate())) {
            LocalDate dateToMark = current;
            AttendanceRecord record = attendanceRecordRepository
                    .findByEmployeeIdAndDate(command.employeeId(), dateToMark)
                    .orElseGet(() -> AttendanceRecord.create(
                            null,
                            command.employeeId(),
                            command.organizationId(),
                            dateToMark,
                            AttendanceStatus.ON_LEAVE
                    ));

            record.markOnLeave();
            attendanceRecordRepository.save(record);

            current = current.plusDays(1);
        }

        return null;
    }
}
