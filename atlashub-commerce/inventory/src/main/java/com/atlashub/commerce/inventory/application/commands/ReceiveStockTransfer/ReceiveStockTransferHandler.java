package com.atlashub.commerce.inventory.application.commands.ReceiveStockTransfer;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.entities.StockTransfer;
import com.atlashub.commerce.inventory.domain.entities.StockTransferItem;
import com.atlashub.commerce.inventory.domain.exceptions.StockTransferNotFoundException;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.commerce.inventory.domain.repositories.StockTransferRepository;
import com.atlashub.commerce.inventory.domain.valueobject.TransferStatus;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class ReceiveStockTransferHandler extends Command<ReceiveStockTransferCommand, Void> {

    private final StockTransferRepository stockTransferRepository;
    private final InventoryRepository inventoryRepository;

    public ReceiveStockTransferHandler(StockTransferRepository stockTransferRepository,
                                       InventoryRepository inventoryRepository) {
        this.stockTransferRepository = Objects.requireNonNull(stockTransferRepository, "StockTransferRepository must not be null");
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "InventoryRepository must not be null");
    }

    @Override
    public Void execute(ReceiveStockTransferCommand command) {
        StockTransfer transfer = stockTransferRepository.findById(command.transferId())
                .orElseThrow(() -> new StockTransferNotFoundException(command.transferId()));

        if (transfer.getStatus() == TransferStatus.APPROVED) {
            transfer.dispatch();
        }

        Map<Long, Integer> receivedMap = new HashMap<>();
        if (command.receivedItems() != null) {
            for (ReceivedTransferItemDto itemDto : command.receivedItems()) {
                receivedMap.put(itemDto.productId(), itemDto.quantityReceived());
            }
        }

        transfer.receive(receivedMap);

        for (StockTransferItem item : transfer.getItems()) {
            int receivedQty = item.getQuantityReceived() != null ? item.getQuantityReceived() : 0;
            if (receivedQty > 0) {
                Optional<Inventory> existing = inventoryRepository.findByOrganizationIdAndOutletIdAndProductId(
                        transfer.getOrganizationId(),
                        transfer.getDestinationOutletId(),
                        item.getProductId()
                );

                if (existing.isPresent()) {
                    Inventory inventory = existing.get();
                    inventory.addStock(receivedQty);
                    inventoryRepository.save(inventory);
                } else {
                    Long newInvId = inventoryRepository.nextIdentity();
                    Inventory newInventory = Inventory.create(
                            newInvId,
                            transfer.getOrganizationId(),
                            transfer.getDestinationOutletId(),
                            item.getProductId(),
                            null,
                            receivedQty,
                            10,
                            5
                    );
                    inventoryRepository.save(newInventory);
                }
            }
        }

        stockTransferRepository.save(transfer);
        return null;
    }
}
