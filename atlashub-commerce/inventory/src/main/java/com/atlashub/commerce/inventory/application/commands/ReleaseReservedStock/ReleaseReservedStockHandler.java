package com.atlashub.commerce.inventory.application.commands.ReleaseReservedStock;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.entities.StockReservation;
import com.atlashub.commerce.inventory.domain.entities.StockReservationItem;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.commerce.inventory.domain.repositories.StockReservationRepository;
import com.atlashub.commerce.inventory.domain.valueobject.ReservationStatus;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Component
public class ReleaseReservedStockHandler extends Command<ReleaseReservedStockCommand, Void> {

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository stockReservationRepository;

    public ReleaseReservedStockHandler(InventoryRepository inventoryRepository,
                                       StockReservationRepository stockReservationRepository) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "InventoryRepository must not be null");
        this.stockReservationRepository = Objects.requireNonNull(stockReservationRepository, "StockReservationRepository must not be null");
    }

    @Override
    @Transactional
    public Void execute(ReleaseReservedStockCommand command) {
        Optional<StockReservation> optRes = stockReservationRepository.findBySalesOrderId(command.salesOrderId());
        if (optRes.isEmpty()) {
            return null;
        }

        StockReservation reservation = optRes.get();
        if (reservation.getStatus() == ReservationStatus.RELEASED
                || reservation.getStatus() == ReservationStatus.FULFILLED
                || reservation.getStatus() == ReservationStatus.FAILED) {
            return null;
        }

        for (StockReservationItem item : reservation.getItems()) {
            Optional<Inventory> optInv = inventoryRepository.findByOrganizationIdAndOutletIdAndProductId(
                    reservation.getOrganizationId(), reservation.getOutletId(), item.getProductId()
            );

            if (optInv.isPresent()) {
                Inventory inv = optInv.get();
                inv.releaseReservedStock(item.getQuantity());
                inventoryRepository.save(inv);
            }
        }

        reservation.release();
        stockReservationRepository.save(reservation);

        return null;
    }
}
