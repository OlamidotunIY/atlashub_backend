package com.atlashub.logistics.warehousing.application.query.ListStockTransfers;

import com.atlashub.logistics.warehousing.application.query.GetStockTransfer.StockTransferResult;
import com.atlashub.logistics.warehousing.domain.repositories.StockTransferRepository;
import com.atlashub.shared.application.usecase.Query;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("warehousingListStockTransfersHandler")
public class ListStockTransfersHandler extends Query<ListStockTransfersQuery, List<StockTransferResult>> {

    private final StockTransferRepository stockTransferRepository;

    public ListStockTransfersHandler(StockTransferRepository stockTransferRepository) {
        this.stockTransferRepository = stockTransferRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockTransferResult> execute(ListStockTransfersQuery query) {
        return stockTransferRepository.findByOrganizationId(query.organizationId())
                .stream()
                .map(transfer -> new StockTransferResult(
                        transfer.getId(),
                        transfer.getOrganizationId(),
                        transfer.getSourceId(),
                        transfer.getSourceType().name(),
                        transfer.getSourceAddress(),
                        transfer.getDestinationId(),
                        transfer.getDestinationType().name(),
                        transfer.getDestinationAddress(),
                        transfer.getTotalWeightKg(),
                        transfer.getTransferShippingFee(),
                        transfer.getStatus().name(),
                        transfer.getShipmentId(),
                        transfer.getItems(),
                        transfer.getRequestedBy(),
                        transfer.getApprovedBy(),
                        transfer.getRequestedAt(),
                        transfer.getReceivedAt()
                ))
                .toList();
    }
}
