package com.atlashub.logistics.warehousing.application.command.ApproveStockTransfer;

import com.atlashub.logistics.warehousing.domain.entities.StockTransfer;
import com.atlashub.logistics.warehousing.domain.exceptions.StockTransferNotFoundException;
import com.atlashub.logistics.warehousing.domain.repositories.StockTransferRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("warehousingApproveStockTransferHandler")
public class ApproveStockTransferHandler extends Command<ApproveStockTransferCommand, ApproveStockTransferResult> {

    private final StockTransferRepository stockTransferRepository;

    public ApproveStockTransferHandler(StockTransferRepository stockTransferRepository) {
        this.stockTransferRepository = stockTransferRepository;
    }

    @Override
    @Transactional
    public ApproveStockTransferResult execute(ApproveStockTransferCommand input) {
        StockTransfer transfer = stockTransferRepository.findById(input.transferId())
                .orElseThrow(() -> new StockTransferNotFoundException(input.transferId()));

        transfer.approve(input.approverId(), input.declaredValue(), input.correlationId());
        stockTransferRepository.save(transfer);

        return new ApproveStockTransferResult(
                transfer.getId(),
                transfer.getStatus().name(),
                transfer.getApprovedBy()
        );
    }
}
