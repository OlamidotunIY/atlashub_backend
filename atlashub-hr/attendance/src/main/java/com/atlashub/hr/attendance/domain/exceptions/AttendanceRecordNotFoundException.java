package com.atlashub.hr.attendance.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class AttendanceRecordNotFoundException extends NotFoundException {

    public AttendanceRecordNotFoundException(Long id) {
        super("Attendance record not found: " + id);
    }
}
