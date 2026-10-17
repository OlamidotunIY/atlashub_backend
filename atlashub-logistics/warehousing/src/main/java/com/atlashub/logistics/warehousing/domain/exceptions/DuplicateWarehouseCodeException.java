package com.atlashub.logistics.warehousing.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class DuplicateWarehouseCodeException extends ConflictException {

    public DuplicateWarehouseCodeException(String code) {
        super("Warehouse code already exists: " + code);
    }
}
