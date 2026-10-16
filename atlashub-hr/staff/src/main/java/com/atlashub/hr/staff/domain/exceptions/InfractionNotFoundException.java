package com.atlashub.hr.staff.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class InfractionNotFoundException extends NotFoundException {

    public InfractionNotFoundException(Long id) {
        super("Infraction not found: " + id);
    }
}
