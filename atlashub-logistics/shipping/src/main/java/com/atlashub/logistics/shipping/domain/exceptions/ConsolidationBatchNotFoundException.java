package com.atlashub.logistics.shipping.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class ConsolidationBatchNotFoundException extends NotFoundException {

    public ConsolidationBatchNotFoundException(Long id) {
        super("Consolidation batch not found: " + id);
    }
}
