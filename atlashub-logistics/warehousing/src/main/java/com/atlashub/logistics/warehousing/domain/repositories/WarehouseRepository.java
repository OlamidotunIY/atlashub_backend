package com.atlashub.logistics.warehousing.domain.repositories;

import com.atlashub.logistics.warehousing.domain.entities.Warehouse;
import com.atlashub.shared.domain.repository.Repository;
import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends Repository<Warehouse> {

    Optional<Warehouse> findByCode(String code);

    Optional<Warehouse> findByOrganizationIdAndCode(Long organizationId, String code);

    List<Warehouse> findByOrganizationId(Long organizationId);
}
