package com.atlashub.commerce.storefront.application.queries.ListPosTransactions;

import com.atlashub.commerce.storefront.application.queries.GetSalesOrder.SalesOrderItemResult;
import com.atlashub.commerce.storefront.application.queries.GetSalesOrder.SalesOrderResult;
import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.entities.SalesOrderItem;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class ListPosTransactionsHandler extends Query<ListPosTransactionsQuery, PageResult<SalesOrderResult>> {

    private final SalesOrderRepository salesOrderRepository;

    public ListPosTransactionsHandler(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = Objects.requireNonNull(salesOrderRepository, "SalesOrderRepository must not be null");
    }

    @Override
    public PageResult<SalesOrderResult> execute(ListPosTransactionsQuery query) {
        Objects.requireNonNull(query, "Query must not be null");

        PageResult<SalesOrder> pagedOrders = salesOrderRepository.findTransactions(
                query.outletId(),
                query.tillId(),
                query.cashierId(),
                query.from(),
                query.to(),
                query.page(),
                query.size()
        );

        List<SalesOrderResult> content = pagedOrders.content().stream()
                .map(this::toResult)
                .toList();

        return new PageResult<>(
                content,
                pagedOrders.pageNumber(),
                pagedOrders.pageSize(),
                pagedOrders.totalElements(),
                pagedOrders.totalPages()
        );
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
