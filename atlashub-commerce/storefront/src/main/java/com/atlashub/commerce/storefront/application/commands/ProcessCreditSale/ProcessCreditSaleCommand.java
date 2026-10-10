package com.atlashub.commerce.storefront.application.commands.ProcessCreditSale;

import com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout.OrderItemDto;

import java.util.List;

public record ProcessCreditSaleCommand(
        Long organizationId,
        Long outletId,
        Long customerId,
        Long cashierId,
        List<OrderItemDto> items
) {
}
