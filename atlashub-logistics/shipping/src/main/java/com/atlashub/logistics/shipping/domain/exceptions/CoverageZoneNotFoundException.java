package com.atlashub.logistics.shipping.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class CoverageZoneNotFoundException extends NotFoundException {

    public CoverageZoneNotFoundException(String zoneCode) {
        super("Coverage zone not found: " + zoneCode);
    }

    public CoverageZoneNotFoundException(Long id) {
        super("Coverage zone not found: " + id);
    }
}
