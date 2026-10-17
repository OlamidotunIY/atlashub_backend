package com.atlashub.logistics.warehousing.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class WarehouseInactiveException extends BusinessRuleException {

    public WarehouseInactiveException(Long id) {
        super("Warehouse is currently inactive: " + id);
    }
}
