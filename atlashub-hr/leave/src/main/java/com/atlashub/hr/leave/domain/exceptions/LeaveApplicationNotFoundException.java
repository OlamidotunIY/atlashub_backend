package com.atlashub.hr.leave.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class LeaveApplicationNotFoundException extends NotFoundException {

    public LeaveApplicationNotFoundException(Long id) {
        super("Leave application not found: " + id);
    }
}
