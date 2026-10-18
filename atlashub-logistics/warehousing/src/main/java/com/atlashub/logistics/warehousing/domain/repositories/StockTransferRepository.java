package com.atlashub.logistics.warehousing.domain.repositories;

import com.atlashub.logistics.warehousing.domain.entities.StockTransfer;
import com.atlashub.shared.domain.repository.Repository;
import java.util.List;
import java.util.Optional;

public interface StockTransferRepository extends Repository<StockTransfer> {

    Optional<StockTransfer> findByShipmentId(Long shipmentId);

    List<StockTransfer> findByOrganizationId(Long organizationId);

    List<StockTransfer> findBySourceIdOrDestinationId(Long sourceId, Long destinationId);
}
