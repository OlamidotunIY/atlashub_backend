package com.atlashub.commerce.inventory.application.commands.ApproveStockTransfer;

import com.atlashub.commerce.inventory.domain.entities.StockTransfer;
import com.atlashub.commerce.inventory.domain.exceptions.StockTransferNotFoundException;
import com.atlashub.commerce.inventory.domain.repositories.StockTransferRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ApproveStockTransferHandler extends Command<ApproveStockTransferCommand, Void> {

    private final StockTransferRepository stockTransferRepository;

    public ApproveStockTransferHandler(StockTransferRepository stockTransferRepository) {
        this.stockTransferRepository = Objects.requireNonNull(stockTransferRepository, "StockTransferRepository must not be null");
    }

    @Override
    @PreAuthorize("hasAuthority('commerce:inventory:adjust')")
    public Void execute(ApproveStockTransferCommand command) {
        StockTransfer transfer = stockTransferRepository.findById(command.transferId())
                .orElseThrow(() -> new StockTransferNotFoundException(command.transferId()));

        transfer.approve();
        stockTransferRepository.save(transfer);
        return null;
    }
}
