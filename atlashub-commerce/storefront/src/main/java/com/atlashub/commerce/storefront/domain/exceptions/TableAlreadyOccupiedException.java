package com.atlashub.commerce.storefront.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class TableAlreadyOccupiedException extends ConflictException {

    public TableAlreadyOccupiedException() {
        super("This table is already occupied");
    }

    public TableAlreadyOccupiedException(Long tableId) {
        super("Table is already occupied: " + tableId);
    }
}
