package com.atlashub.commerce.inventory.application.commands.CreateCustomerReturn;

import com.atlashub.commerce.inventory.domain.valueobject.RefundMethod;
import com.atlashub.shared.domain.valueobject.Money;

import java.util.List;

public record CreateCustomerReturnCommand(
        Long organizationId,
        Long outletId,
        Long salesOrderId,
        Long customerId,
        Money refundAmount,
        String reason,
        RefundMethod refundMethod,
        List<ReturnItemDto> items
) {
}
