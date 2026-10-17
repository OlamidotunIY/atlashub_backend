package com.atlashub.logistics.warehousing.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class StockTransferNotFoundException extends NotFoundException {

    public StockTransferNotFoundException(Long id) {
        super("Stock transfer not found: " + id);
    }
}
