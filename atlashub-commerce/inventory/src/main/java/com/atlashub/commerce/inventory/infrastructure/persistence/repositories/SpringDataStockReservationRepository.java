package com.atlashub.commerce.inventory.infrastructure.persistence.repositories;

import com.atlashub.commerce.inventory.domain.valueobject.ReservationStatus;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockReservationJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataStockReservationRepository extends JpaRepository<StockReservationJpa, Long> {

    Optional<StockReservationJpa> findBySalesOrderId(Long salesOrderId);

    List<StockReservationJpa> findByOrganizationIdAndStatus(Long organizationId, ReservationStatus status);
}
