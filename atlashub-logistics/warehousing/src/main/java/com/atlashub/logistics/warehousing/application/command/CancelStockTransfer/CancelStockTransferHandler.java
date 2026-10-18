package com.atlashub.logistics.warehousing.application.command.CancelStockTransfer;

import com.atlashub.logistics.warehousing.domain.entities.StockTransfer;
import com.atlashub.logistics.warehousing.domain.exceptions.StockTransferNotFoundException;
import com.atlashub.logistics.warehousing.domain.repositories.StockTransferRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CancelStockTransferHandler extends Command<CancelStockTransferCommand, CancelStockTransferResult> {

    private final StockTransferRepository stockTransferRepository;

    public CancelStockTransferHandler(StockTransferRepository stockTransferRepository) {
        this.stockTransferRepository = stockTransferRepository;
    }

    @Override
    @Transactional
    public CancelStockTransferResult execute(CancelStockTransferCommand input) {
        StockTransfer transfer = stockTransferRepository.findById(input.transferId())
                .orElseThrow(() -> new StockTransferNotFoundException(input.transferId()));

        transfer.cancel(input.reason(), input.correlationId());
        stockTransferRepository.save(transfer);

        return new CancelStockTransferResult(
                transfer.getId(),
                transfer.getStatus().name(),
                input.reason()
        );
    }
}
