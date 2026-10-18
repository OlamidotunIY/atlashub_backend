package com.atlashub.hr.attendance.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class AttendanceInvariantException extends BusinessRuleException {

    public AttendanceInvariantException(String message) {
        super(message);
    }
}
