package com.atlashub.hr.attendance.domain.entities;

import com.atlashub.hr.attendance.domain.exceptions.AlreadyClockedInException;
import com.atlashub.hr.attendance.domain.exceptions.AttendanceInvariantException;
import com.atlashub.hr.attendance.domain.exceptions.NotClockedInException;
import com.atlashub.hr.attendance.domain.valueobject.AttendanceStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Getter
public class AttendanceRecord extends AggregateRoot<Long> {

    private final Long id;
    private final Long employeeId;
    private final Long organizationId;
    private final LocalDate date;
    private ZonedDateTime clockIn;
    private ZonedDateTime clockOut;
    private BigDecimal hoursWorked;
    private BigDecimal overtimeHours;
    private AttendanceStatus status;

    public AttendanceRecord(
            Long id,
            Long employeeId,
            Long organizationId,
            LocalDate date,
            ZonedDateTime clockIn,
            ZonedDateTime clockOut,
            BigDecimal hoursWorked,
            BigDecimal overtimeHours,
            AttendanceStatus status
    ) {
        this.id = id;
        this.employeeId = employeeId;
        this.organizationId = organizationId;
        this.date = date;
        this.clockIn = clockIn;
        this.clockOut = clockOut;
        this.hoursWorked = hoursWorked;
        this.overtimeHours = overtimeHours;
        this.status = status;
    }

    public static AttendanceRecord create(
            Long id,
            Long employeeId,
            Long organizationId,
            LocalDate date,
            AttendanceStatus status
    ) {
        if (employeeId == null) {
            throw new AttendanceInvariantException("Employee ID must not be null");
        }
        if (organizationId == null) {
            throw new AttendanceInvariantException("Organization ID must not be null");
        }
        if (date == null) {
            throw new AttendanceInvariantException("Date must not be null");
        }

        return new AttendanceRecord(
                id,
                employeeId,
                organizationId,
                date,
                null,
                null,
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                status != null ? status : AttendanceStatus.PRESENT
        );
    }

    public void clockIn(ZonedDateTime time, ZonedDateTime shiftStart) {
        if (this.clockIn != null) {
            throw new AlreadyClockedInException();
        }
        if (time == null) {
            throw new AttendanceInvariantException("Clock in time must not be null");
        }

        this.clockIn = time;
        if (shiftStart != null && time.isAfter(shiftStart)) {
            this.status = AttendanceStatus.LATE;
        } else {
            this.status = AttendanceStatus.PRESENT;
        }
    }

    public void clockOut(ZonedDateTime time, int shiftHours) {
        if (this.clockIn == null) {
            throw new NotClockedInException();
        }
        if (time == null) {
            throw new AttendanceInvariantException("Clock out time must not be null");
        }
        if (time.isBefore(this.clockIn)) {
            throw new AttendanceInvariantException("Clock out time cannot be before clock in time");
        }

        this.clockOut = time;
        long minutes = ChronoUnit.MINUTES.between(this.clockIn, time);
        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        this.hoursWorked = hours;

        BigDecimal shiftHoursBd = BigDecimal.valueOf(shiftHours);
        if (hours.compareTo(shiftHoursBd) > 0) {
            this.overtimeHours = hours.subtract(shiftHoursBd).setScale(2, RoundingMode.HALF_UP);
        } else {
            this.overtimeHours = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }

    public void markOnLeave() {
        this.status = AttendanceStatus.ON_LEAVE;
    }

    @Override
    public Long getId() {
        return id;
    }
}
