package com.atlashub.commerce.inventory.application.commands.ReserveStockForOrder;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.entities.StockReservation;
import com.atlashub.commerce.inventory.domain.entities.StockReservationItem;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.commerce.inventory.domain.repositories.StockReservationRepository;
import com.atlashub.commerce.inventory.domain.valueobject.ReservationStatus;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class ReserveStockForOrderHandler extends Command<ReserveStockForOrderCommand, ReserveStockForOrderResult> {

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository stockReservationRepository;

    public ReserveStockForOrderHandler(InventoryRepository inventoryRepository,
                                       StockReservationRepository stockReservationRepository) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "InventoryRepository must not be null");
        this.stockReservationRepository = Objects.requireNonNull(stockReservationRepository, "StockReservationRepository must not be null");
    }

    @Override
    @Transactional
    public ReserveStockForOrderResult execute(ReserveStockForOrderCommand command) {
        Optional<StockReservation> existing = stockReservationRepository.findBySalesOrderId(command.salesOrderId());
        if (existing.isPresent()) {
            StockReservation reservation = existing.get();
            boolean isSuccessful = reservation.getStatus() == ReservationStatus.ACTIVE
                    || reservation.getStatus() == ReservationStatus.FULFILLED;
            return new ReserveStockForOrderResult(
                    reservation.getId(),
                    reservation.getStatus(),
                    isSuccessful,
                    "Reservation already processed for sales order " + command.salesOrderId()
            );
        }

        Long reservationId = stockReservationRepository.nextIdentity();
        List<StockReservationItem> reservationItems = new ArrayList<>();
        if (command.items() != null) {
            for (OrderItemDto item : command.items()) {
                reservationItems.add(StockReservationItem.create(
                        stockReservationRepository.nextIdentity(),
                        reservationId,
                        item.productId(),
                        item.quantity()
                ));
            }
        }

        StockReservation reservation = StockReservation.create(
                reservationId,
                command.salesOrderId(),
                command.organizationId(),
                command.outletId(),
                reservationItems
        );

        Map<Long, Inventory> lockedInventories = new HashMap<>();
        String failureReason = null;

        if (command.items() == null || command.items().isEmpty()) {
            failureReason = "No items requested for reservation";
        } else {
            for (OrderItemDto item : command.items()) {
                Optional<Inventory> optInv = inventoryRepository.findByOrganizationIdAndOutletIdAndProductId(
                        command.organizationId(), command.outletId(), item.productId()
                );

                if (optInv.isEmpty()) {
                    failureReason = "Inventory record missing for product " + item.productId();
                    break;
                }

                Inventory inv = optInv.get();
                int available = inv.getQuantity() - inv.getReservedQuantity();
                if (available < item.quantity()) {
                    failureReason = "Insufficient stock for product " + item.productId()
                            + ": available " + available + ", requested " + item.quantity();
                    break;
                }

                lockedInventories.put(item.productId(), inv);
            }
        }

        if (failureReason != null) {
            reservation.fail(failureReason);
            stockReservationRepository.save(reservation);
            return new ReserveStockForOrderResult(
                    reservation.getId(),
                    ReservationStatus.FAILED,
                    false,
                    failureReason
            );
        }

        for (OrderItemDto item : command.items()) {
            Inventory inv = lockedInventories.get(item.productId());
            inv.reserveStock(item.quantity());
            inventoryRepository.save(inv);
        }

        reservation.confirm();
        stockReservationRepository.save(reservation);

        return new ReserveStockForOrderResult(
                reservation.getId(),
                ReservationStatus.ACTIVE,
                true,
                "Stock reserved successfully"
        );
    }
}
