package com.atlashub.commerce.inventory.application.queries.ListStockTransfers;

import com.atlashub.commerce.inventory.domain.entities.StockTransfer;
import com.atlashub.commerce.inventory.domain.entities.StockTransferItem;
import com.atlashub.commerce.inventory.domain.repositories.StockTransferRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ListStockTransfersHandler extends Query<ListStockTransfersQuery, PageResult<StockTransferResult>> {

    private final StockTransferRepository stockTransferRepository;

    public ListStockTransfersHandler(StockTransferRepository stockTransferRepository) {
        this.stockTransferRepository = Objects.requireNonNull(stockTransferRepository, "StockTransferRepository must not be null");
    }

    @Override
    public PageResult<StockTransferResult> execute(ListStockTransfersQuery query) {
        PageResult<StockTransfer> page = stockTransferRepository.findByOrganizationId(
                query.organizationId(),
                query.status(),
                query.page(),
                query.size()
        );

        List<StockTransferResult> results = page.content().stream()
                .map(this::toResult)
                .toList();

        return new PageResult<>(
                results,
                page.pageNumber(),
                page.pageSize(),
                page.totalElements(),
                page.totalPages()
        );
    }

    private StockTransferResult toResult(StockTransfer transfer) {
        List<StockTransferItemResult> itemResults = transfer.getItems().stream()
                .map(this::toItemResult)
                .toList();

        return new StockTransferResult(
                transfer.getId(),
                transfer.getOrganizationId(),
                transfer.getSourceOutletId(),
                transfer.getDestinationOutletId(),
                transfer.getStatus(),
                itemResults,
                transfer.getRequestedAt(),
                transfer.getReceivedAt()
        );
    }

    private StockTransferItemResult toItemResult(StockTransferItem item) {
        return new StockTransferItemResult(
                item.getId(),
                item.getProductId(),
                item.getQuantityRequested(),
                item.getQuantityReceived()
        );
    }
}
