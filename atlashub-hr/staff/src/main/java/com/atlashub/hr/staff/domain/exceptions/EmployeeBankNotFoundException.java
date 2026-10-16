package com.atlashub.hr.staff.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class EmployeeBankNotFoundException extends NotFoundException {

    public EmployeeBankNotFoundException(Long id) {
        super("Employee bank not found: " + id);
    }
}
