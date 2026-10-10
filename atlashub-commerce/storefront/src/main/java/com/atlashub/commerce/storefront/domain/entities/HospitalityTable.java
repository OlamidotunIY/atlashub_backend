package com.atlashub.commerce.storefront.domain.entities;

import com.atlashub.commerce.storefront.domain.exceptions.InvalidOrderStateException;
import com.atlashub.commerce.storefront.domain.exceptions.TableAlreadyOccupiedException;
import com.atlashub.commerce.storefront.domain.valueobject.TableStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.util.Objects;

@Getter
public class HospitalityTable extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long outletId;
    private final String tableNumber;
    private Integer covers;
    private TableStatus status;
    private Long currentOrderId;

    public HospitalityTable(
            Long id,
            Long organizationId,
            Long outletId,
            String tableNumber,
            Integer covers,
            TableStatus status,
            Long currentOrderId
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.outletId = outletId;
        this.tableNumber = tableNumber;
        this.covers = covers;
        this.status = status;
        this.currentOrderId = currentOrderId;
    }

    public static HospitalityTable create(
            Long id,
            Long organizationId,
            Long outletId,
            String tableNumber,
            Integer covers
    ) {
        Objects.requireNonNull(id, "Table ID must not be null");
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        Objects.requireNonNull(outletId, "Outlet ID must not be null");
        Objects.requireNonNull(tableNumber, "Table number must not be null");

        return new HospitalityTable(
                id,
                organizationId,
                outletId,
                tableNumber,
                covers != null ? covers : 0,
                TableStatus.AVAILABLE,
                null
        );
    }

    public void occupy(Long orderId, int occupiedCovers) {
        if (status != TableStatus.AVAILABLE) {
            throw new TableAlreadyOccupiedException(id);
        }
        if (occupiedCovers <= 0) {
            throw new InvalidOrderStateException("Covers must be greater than zero");
        }
        this.status = TableStatus.OCCUPIED;
        this.currentOrderId = orderId;
        this.covers = occupiedCovers;
    }

    public void requestBill() {
        if (status != TableStatus.OCCUPIED) {
            throw new InvalidOrderStateException("Cannot request bill for table that is not occupied: " + status);
        }
        this.status = TableStatus.BILL_REQUESTED;
    }

    public void clear() {
        this.status = TableStatus.AVAILABLE;
        this.currentOrderId = null;
    }

    @Override
    public Long getId() {
        return id;
    }
}
