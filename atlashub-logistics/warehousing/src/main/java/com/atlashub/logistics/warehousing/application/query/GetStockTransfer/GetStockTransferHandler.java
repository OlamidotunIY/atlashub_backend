package com.atlashub.logistics.warehousing.application.query.GetStockTransfer;

import com.atlashub.logistics.warehousing.domain.entities.StockTransfer;
import com.atlashub.logistics.warehousing.domain.exceptions.StockTransferNotFoundException;
import com.atlashub.logistics.warehousing.domain.repositories.StockTransferRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetStockTransferHandler extends Query<GetStockTransferQuery, StockTransferResult> {

    private final StockTransferRepository stockTransferRepository;

    public GetStockTransferHandler(StockTransferRepository stockTransferRepository) {
        this.stockTransferRepository = stockTransferRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public StockTransferResult execute(GetStockTransferQuery query) {
        StockTransfer transfer = stockTransferRepository.findById(query.transferId())
                .orElseThrow(() -> new StockTransferNotFoundException(query.transferId()));

        return new StockTransferResult(
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
        );
    }
}
