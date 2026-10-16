package com.atlashub.hr.attendance.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class AlreadyClockedInException extends ConflictException {

    public AlreadyClockedInException() {
        super("Employee has already clocked in today");
    }
}
