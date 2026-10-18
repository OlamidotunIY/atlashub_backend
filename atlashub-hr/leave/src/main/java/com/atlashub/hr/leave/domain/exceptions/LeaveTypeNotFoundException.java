package com.atlashub.hr.leave.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class LeaveTypeNotFoundException extends NotFoundException {

    public LeaveTypeNotFoundException(Long id) {
        super("Leave type not found: " + id);
    }
}
