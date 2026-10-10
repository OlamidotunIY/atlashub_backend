package com.atlashub.commerce.catalog.application.queries.ListPurchaseOrders;

import com.atlashub.commerce.catalog.domain.entities.PurchaseOrder;
import com.atlashub.commerce.catalog.domain.repositories.PurchaseOrderRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ListPurchaseOrdersHandler extends Query<ListPurchaseOrdersQuery, PageResult<PurchaseOrderResult>> {

    private final PurchaseOrderRepository purchaseOrderRepository;

    public ListPurchaseOrdersHandler(PurchaseOrderRepository purchaseOrderRepository) {
        this.purchaseOrderRepository = Objects.requireNonNull(purchaseOrderRepository, "PurchaseOrderRepository must not be null");
    }

    @Override
    public PageResult<PurchaseOrderResult> execute(ListPurchaseOrdersQuery query) {
        PageResult<PurchaseOrder> page = purchaseOrderRepository.findByOrganizationId(
                query.organizationId(),
                query.status(),
                query.page(),
                query.size()
        );

        List<PurchaseOrderResult> results = page.content().stream()
                .map(this::toResult)
                .toList();

        return new PageResult<>(results, page.pageNumber(), page.pageSize(), page.totalElements(), page.totalPages());
    }

    private PurchaseOrderResult toResult(PurchaseOrder po) {
        return new PurchaseOrderResult(
                po.getId(),
                po.getOrganizationId(),
                po.getOutletId(),
                po.getSupplierId(),
                po.getStatus(),
                po.getTotalAmount(),
                po.getExpectedDeliveryDate(),
                po.getItems() != null ? po.getItems().size() : 0,
                po.getCreatedAt()
        );
    }
}
