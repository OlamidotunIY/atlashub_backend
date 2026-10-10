package com.atlashub.commerce.inventory.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class StockCountNotFoundException extends NotFoundException {

    public StockCountNotFoundException(Long id) {
        super("Stock count not found: " + id);
    }
}
