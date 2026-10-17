package com.atlashub.logistics.warehousing.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class WarehouseNotFoundException extends NotFoundException {

    public WarehouseNotFoundException(Long id) {
        super("Warehouse not found: " + id);
    }

    public WarehouseNotFoundException(String code) {
        super("Warehouse not found for code: " + code);
    }
}
