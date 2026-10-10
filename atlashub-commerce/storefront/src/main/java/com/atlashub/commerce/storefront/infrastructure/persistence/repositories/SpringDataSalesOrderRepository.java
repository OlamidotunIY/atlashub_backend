package com.atlashub.commerce.storefront.infrastructure.persistence.repositories;

import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.SalesOrderJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataSalesOrderRepository extends JpaRepository<SalesOrderJpa, Long> {

    Optional<SalesOrderJpa> findByChargeReference(String chargeReference);

    List<SalesOrderJpa> findByOutletId(Long outletId);

    List<SalesOrderJpa> findByStatusAndSaleDateBefore(OrderStatus status, ZonedDateTime cutoff);

    @Query("SELECT s FROM SalesOrderJpa s WHERE s.outletId = :outletId " +
            "AND (:tillId IS NULL OR s.tillId = :tillId) " +
            "AND (:cashierId IS NULL OR s.cashierId = :cashierId) " +
            "AND (:from IS NULL OR s.saleDate >= :from) " +
            "AND (:to IS NULL OR s.saleDate <= :to) " +
            "ORDER BY s.saleDate DESC")
    Page<SalesOrderJpa> findTransactions(
            @Param("outletId") Long outletId,
            @Param("tillId") Long tillId,
            @Param("cashierId") Long cashierId,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to,
            Pageable pageable
    );
}
