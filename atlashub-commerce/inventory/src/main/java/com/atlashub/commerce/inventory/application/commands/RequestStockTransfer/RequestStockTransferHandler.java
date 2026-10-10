package com.atlashub.commerce.inventory.application.commands.RequestStockTransfer;

import com.atlashub.commerce.inventory.domain.entities.StockTransfer;
import com.atlashub.commerce.inventory.domain.repositories.StockTransferRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RequestStockTransferHandler extends Command<RequestStockTransferCommand, RequestStockTransferResult> {

    private final StockTransferRepository stockTransferRepository;

    public RequestStockTransferHandler(StockTransferRepository stockTransferRepository) {
        this.stockTransferRepository = Objects.requireNonNull(stockTransferRepository, "StockTransferRepository must not be null");
    }

    @Override
    public RequestStockTransferResult execute(RequestStockTransferCommand command) {
        Long transferId = stockTransferRepository.nextIdentity();
        StockTransfer transfer = StockTransfer.create(
                transferId,
                command.organizationId(),
                command.sourceOutletId(),
                command.destinationOutletId()
        );

        if (command.items() != null) {
            for (TransferItemDto itemDto : command.items()) {
                Long itemId = stockTransferRepository.nextIdentity();
                transfer.addItem(itemId, itemDto.productId(), itemDto.quantityRequested());
            }
        }

        stockTransferRepository.save(transfer);
        return new RequestStockTransferResult(transferId);
    }
}
