package com.atlashub.commerce.storefront.application.queries.GetSalesOrder;

import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;
import com.atlashub.commerce.storefront.domain.valueobject.OrderType;
import com.atlashub.commerce.storefront.domain.valueobject.PaymentMethod;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.ZonedDateTime;
import java.util.List;

public record SalesOrderResult(
        Long id,
        Long organizationId,
        Long outletId,
        Long vendorId,
        Long customerId,
        Long cashierId,
        Long tillId,
        OrderType type,
        OrderStatus status,
        List<SalesOrderItemResult> items,
        Long discountId,
        Money totalGross,
        Money totalDiscount,
        Money totalTax,
        Money totalNet,
        PaymentMethod paymentMethod,
        String chargeReference,
        ZonedDateTime saleDate,
        String deliveryAddress
) {
}
