package com.atlashub.logistics.warehousing.application.query.GetWarehouse;

import com.atlashub.logistics.warehousing.domain.entities.Warehouse;
import com.atlashub.logistics.warehousing.domain.exceptions.WarehouseNotFoundException;
import com.atlashub.logistics.warehousing.domain.repositories.WarehouseRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetWarehouseHandler extends Query<GetWarehouseQuery, WarehouseResult> {

    private final WarehouseRepository warehouseRepository;

    public GetWarehouseHandler(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResult execute(GetWarehouseQuery query) {
        Warehouse warehouse = warehouseRepository.findById(query.warehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException(query.warehouseId()));

        return new WarehouseResult(
                warehouse.getId(),
                warehouse.getOrganizationId(),
                warehouse.getName(),
                warehouse.getCode(),
                warehouse.getFacilityType().name(),
                warehouse.getAddress(),
                warehouse.getManagerId(),
                warehouse.getStatus().name(),
                warehouse.getCreatedAt()
        );
    }
}
