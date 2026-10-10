package com.atlashub.commerce.storefront.application.queries.GetSalesOrder;

import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.entities.SalesOrderItem;
import com.atlashub.commerce.storefront.domain.exceptions.SalesOrderNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class GetSalesOrderHandler extends Query<GetSalesOrderQuery, SalesOrderResult> {

    private final SalesOrderRepository salesOrderRepository;

    public GetSalesOrderHandler(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = Objects.requireNonNull(salesOrderRepository, "SalesOrderRepository must not be null");
    }

    @Override
    public SalesOrderResult execute(GetSalesOrderQuery query) {
        Objects.requireNonNull(query, "Query must not be null");

        SalesOrder order = salesOrderRepository.findByIdWithItems(query.salesOrderId())
                .or(() -> salesOrderRepository.findById(query.salesOrderId()))
                .orElseThrow(() -> new SalesOrderNotFoundException(query.salesOrderId()));

        return toResult(order);
    }

    private SalesOrderResult toResult(SalesOrder order) {
        List<SalesOrderItemResult> itemResults = order.getItems() == null
                ? Collections.emptyList()
                : order.getItems().stream().map(this::toItemResult).toList();

        return new SalesOrderResult(
                order.getId(),
                order.getOrganizationId(),
                order.getOutletId(),
                order.getVendorId(),
                order.getCustomerId(),
                order.getCashierId(),
                order.getTillId(),
                order.getType(),
                order.getStatus(),
                itemResults,
                order.getDiscountId(),
                order.getTotalGross(),
                order.getTotalDiscount(),
                order.getTotalTax(),
                order.getTotalNet(),
                order.getPaymentMethod(),
                order.getChargeReference(),
                order.getSaleDate(),
                order.getDeliveryAddress()
        );
    }

    private SalesOrderItemResult toItemResult(SalesOrderItem item) {
        return new SalesOrderItemResult(
                item.getId(),
                item.getProductId(),
                item.getVariantId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice(),
                item.getTaxAmount(),
                item.getDiscountAmount()
        );
    }
}
