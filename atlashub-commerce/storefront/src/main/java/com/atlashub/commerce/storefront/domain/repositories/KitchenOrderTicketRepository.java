package com.atlashub.commerce.storefront.domain.repositories;

import com.atlashub.commerce.storefront.domain.entities.KitchenOrderTicket;
import com.atlashub.commerce.storefront.domain.valueobject.KotStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface KitchenOrderTicketRepository extends Repository<KitchenOrderTicket> {

    Optional<KitchenOrderTicket> findByIdWithItems(Long id);

    List<KitchenOrderTicket> findBySalesOrderId(Long salesOrderId);

    List<KitchenOrderTicket> findByOutletIdAndStatus(Long outletId, KotStatus status);
}
