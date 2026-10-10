package com.atlashub.commerce.inventory.domain.repositories;

import com.atlashub.commerce.inventory.domain.entities.StockReservation;
import com.atlashub.commerce.inventory.domain.valueobject.ReservationStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface StockReservationRepository extends Repository<StockReservation> {

    Optional<StockReservation> findBySalesOrderId(Long salesOrderId);

    List<StockReservation> findByOrganizationIdAndStatus(Long organizationId, ReservationStatus status);
}
