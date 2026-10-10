package com.atlashub.commerce.inventory.infrastructure.persistence.repositories;

import com.atlashub.commerce.inventory.domain.valueobject.TransferStatus;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockTransferJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataStockTransferRepository extends JpaRepository<StockTransferJpa, Long> {

    List<StockTransferJpa> findByOrganizationIdAndStatus(Long organizationId, TransferStatus status);

    Page<StockTransferJpa> findByOrganizationIdAndStatus(Long organizationId, TransferStatus status, Pageable pageable);

    Page<StockTransferJpa> findByOrganizationId(Long organizationId, Pageable pageable);
}
