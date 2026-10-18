package com.atlashub.hr.staff.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class EmployeeNotFoundException extends NotFoundException {

    public EmployeeNotFoundException(Long id) {
        super("Employee not found: " + id);
    }
}
