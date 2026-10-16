package com.atlashub.hr.attendance.application.query.GetAttendanceSummary;

import com.atlashub.hr.attendance.domain.entities.AttendanceRecord;
import com.atlashub.hr.attendance.domain.repositories.AttendanceRecordRepository;
import com.atlashub.hr.attendance.domain.valueobject.AttendanceStatus;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Component
public class GetAttendanceSummaryHandler extends Query<GetAttendanceSummaryQuery, AttendanceSummaryResult> {

    private final AttendanceRecordRepository attendanceRecordRepository;

    public GetAttendanceSummaryHandler(AttendanceRecordRepository attendanceRecordRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    @Override
    public AttendanceSummaryResult execute(GetAttendanceSummaryQuery query) {
        LocalDate from = LocalDate.of(query.year(), query.month(), 1);
        LocalDate to = from.plusMonths(1).minusDays(1);

        List<AttendanceRecord> records = attendanceRecordRepository.findByOrganizationIdAndEmployeeIdAndDateBetween(
                query.organizationId(),
                query.employeeId(),
                from,
                to
        );

        int totalDays = to.getDayOfMonth();
        int presentDays = 0;
        int absentDays = 0;
        int lateDays = 0;
        BigDecimal totalHoursWorked = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalOvertimeHours = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (AttendanceRecord record : records) {
            if (record.getStatus() == AttendanceStatus.PRESENT) {
                presentDays++;
            } else if (record.getStatus() == AttendanceStatus.LATE) {
                lateDays++;
                presentDays++;
            } else if (record.getStatus() == AttendanceStatus.ABSENT) {
                absentDays++;
            }

            if (record.getHoursWorked() != null) {
                totalHoursWorked = totalHoursWorked.add(record.getHoursWorked());
            }
            if (record.getOvertimeHours() != null) {
                totalOvertimeHours = totalOvertimeHours.add(record.getOvertimeHours());
            }
        }

        List<AttendanceDayResult> dayResults = records.stream()
                .map(r -> new AttendanceDayResult(
                        r.getDate(),
                        r.getClockIn(),
                        r.getClockOut(),
                        r.getHoursWorked(),
                        r.getOvertimeHours(),
                        r.getStatus()
                ))
                .toList();

        return new AttendanceSummaryResult(
                totalDays,
                presentDays,
                absentDays,
                lateDays,
                totalHoursWorked,
                totalOvertimeHours,
                dayResults
        );
    }
}
