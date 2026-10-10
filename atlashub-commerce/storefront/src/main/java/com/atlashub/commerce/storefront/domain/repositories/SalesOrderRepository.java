package com.atlashub.commerce.storefront.domain.repositories;

import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;
import com.atlashub.shared.domain.repository.Repository;

import com.atlashub.shared.domain.valueobject.PageResult;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface SalesOrderRepository extends Repository<SalesOrder> {

    Optional<SalesOrder> findByIdWithItems(Long id);

    Optional<SalesOrder> findByChargeReference(String chargeReference);

    List<SalesOrder> findByOutletId(Long outletId);

    List<SalesOrder> findByStatusAndSaleDateBefore(OrderStatus status, ZonedDateTime cutoff);

    PageResult<SalesOrder> findTransactions(
            Long outletId,
            Long tillId,
            Long cashierId,
            ZonedDateTime from,
            ZonedDateTime to,
            int page,
            int size
    );
}

