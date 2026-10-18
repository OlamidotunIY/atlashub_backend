package com.atlashub.logistics.warehousing.application.command.DispatchStockTransfer;

import com.atlashub.logistics.warehousing.domain.entities.StockTransfer;
import com.atlashub.logistics.warehousing.domain.exceptions.StockTransferNotFoundException;
import com.atlashub.logistics.warehousing.domain.repositories.StockTransferRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DispatchStockTransferHandler extends Command<DispatchStockTransferCommand, DispatchStockTransferResult> {

    private final StockTransferRepository stockTransferRepository;

    public DispatchStockTransferHandler(StockTransferRepository stockTransferRepository) {
        this.stockTransferRepository = stockTransferRepository;
    }

    @Override
    @Transactional
    public DispatchStockTransferResult execute(DispatchStockTransferCommand input) {
        StockTransfer transfer = stockTransferRepository.findById(input.transferId())
                .orElseThrow(() -> new StockTransferNotFoundException(input.transferId()));

        transfer.markDispatched(input.shipmentId(), input.correlationId());
        stockTransferRepository.save(transfer);

        return new DispatchStockTransferResult(
                transfer.getId(),
                transfer.getStatus().name(),
                transfer.getShipmentId()
        );
    }
}
