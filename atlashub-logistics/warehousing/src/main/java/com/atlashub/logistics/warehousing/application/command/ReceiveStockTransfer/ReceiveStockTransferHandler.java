package com.atlashub.logistics.warehousing.application.command.ReceiveStockTransfer;

import com.atlashub.logistics.warehousing.domain.entities.StockTransfer;
import com.atlashub.logistics.warehousing.domain.exceptions.StockTransferNotFoundException;
import com.atlashub.logistics.warehousing.domain.repositories.StockTransferRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReceiveStockTransferHandler extends Command<ReceiveStockTransferCommand, ReceiveStockTransferResult> {

    private final StockTransferRepository stockTransferRepository;

    public ReceiveStockTransferHandler(StockTransferRepository stockTransferRepository) {
        this.stockTransferRepository = stockTransferRepository;
    }

    @Override
    @Transactional
    public ReceiveStockTransferResult execute(ReceiveStockTransferCommand input) {
        StockTransfer transfer = stockTransferRepository.findById(input.transferId())
                .orElseThrow(() -> new StockTransferNotFoundException(input.transferId()));

        transfer.receive(input.receivedAt(), input.correlationId());
        stockTransferRepository.save(transfer);

        return new ReceiveStockTransferResult(
                transfer.getId(),
                transfer.getStatus().name(),
                transfer.getReceivedAt()
        );
    }
}
