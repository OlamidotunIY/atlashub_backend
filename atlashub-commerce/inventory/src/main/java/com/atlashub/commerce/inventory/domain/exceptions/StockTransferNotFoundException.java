package com.atlashub.commerce.inventory.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class StockTransferNotFoundException extends NotFoundException {

    public StockTransferNotFoundException(Long id) {
        super("Stock transfer not found: " + id);
    }
}
