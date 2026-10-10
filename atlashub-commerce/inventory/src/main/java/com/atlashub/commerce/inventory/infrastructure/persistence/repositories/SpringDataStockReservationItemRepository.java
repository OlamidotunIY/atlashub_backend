package com.atlashub.commerce.inventory.infrastructure.persistence.repositories;

import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockReservationItemJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataStockReservationItemRepository extends JpaRepository<StockReservationItemJpa, Long> {

    List<StockReservationItemJpa> findByReservationId(Long reservationId);

    void deleteByReservationId(Long reservationId);
}
