package com.atlashub.commerce.storefront.infrastructure.persistence.repositories;

import com.atlashub.commerce.storefront.infrastructure.persistence.entities.SalesOrderItemJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataSalesOrderItemRepository extends JpaRepository<SalesOrderItemJpa, Long> {

    List<SalesOrderItemJpa> findBySalesOrderId(Long salesOrderId);

    void deleteBySalesOrderId(Long salesOrderId);
}
