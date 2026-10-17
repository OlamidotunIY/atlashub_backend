package com.atlashub.logistics.warehousing.application.query.ListWarehouses;

import com.atlashub.logistics.warehousing.application.query.GetWarehouse.WarehouseResult;
import com.atlashub.logistics.warehousing.domain.repositories.WarehouseRepository;
import com.atlashub.shared.application.usecase.Query;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ListWarehousesHandler extends Query<ListWarehousesQuery, List<WarehouseResult>> {

    private final WarehouseRepository warehouseRepository;

    public ListWarehousesHandler(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResult> execute(ListWarehousesQuery query) {
        return warehouseRepository.findByOrganizationId(query.organizationId())
                .stream()
                .map(w -> new WarehouseResult(
                        w.getId(),
                        w.getOrganizationId(),
                        w.getName(),
                        w.getCode(),
                        w.getFacilityType().name(),
                        w.getAddress(),
                        w.getManagerId(),
                        w.getStatus().name(),
                        w.getCreatedAt()
                ))
                .toList();
    }
}
