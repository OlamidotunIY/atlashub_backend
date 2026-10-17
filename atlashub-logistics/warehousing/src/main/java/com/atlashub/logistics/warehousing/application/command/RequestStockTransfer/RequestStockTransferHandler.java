package com.atlashub.logistics.warehousing.application.command.RequestStockTransfer;

import com.atlashub.logistics.warehousing.domain.entities.StockTransfer;
import com.atlashub.logistics.warehousing.domain.repositories.StockTransferRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RequestStockTransferHandler extends Command<RequestStockTransferCommand, RequestStockTransferResult> {

    private final StockTransferRepository stockTransferRepository;

    public RequestStockTransferHandler(StockTransferRepository stockTransferRepository) {
        this.stockTransferRepository = stockTransferRepository;
    }

    @Override
    @Transactional
    public RequestStockTransferResult execute(RequestStockTransferCommand input) {
        Long id = stockTransferRepository.nextIdentity();

        StockTransfer transfer = StockTransfer.create(
                id,
                input.organizationId(),
                input.sourceId(),
                input.sourceType(),
                input.sourceAddress(),
                input.destinationId(),
                input.destinationType(),
                input.destinationAddress(),
                input.transferShippingFee(),
                input.items(),
                input.requestedBy(),
                input.correlationId()
        );

        stockTransferRepository.save(transfer);

        return new RequestStockTransferResult(
                transfer.getId(),
                transfer.getStatus().name(),
                transfer.getTotalWeightKg(),
                transfer.getTransferShippingFee()
        );
    }
}
