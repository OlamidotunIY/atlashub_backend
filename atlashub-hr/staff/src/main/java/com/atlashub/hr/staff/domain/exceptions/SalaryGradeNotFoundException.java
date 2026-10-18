package com.atlashub.hr.staff.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class SalaryGradeNotFoundException extends NotFoundException {

    public SalaryGradeNotFoundException(Long id) {
        super("Salary grade not found: " + id);
    }
}
