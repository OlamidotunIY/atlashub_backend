package com.atlashub.logistics.warehousing.application.command.RegisterWarehouse;

import com.atlashub.logistics.warehousing.domain.entities.Warehouse;
import com.atlashub.logistics.warehousing.domain.exceptions.DuplicateWarehouseCodeException;
import com.atlashub.logistics.warehousing.domain.repositories.WarehouseRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RegisterWarehouseHandler extends Command<RegisterWarehouseCommand, RegisterWarehouseResult> {

    private final WarehouseRepository warehouseRepository;

    public RegisterWarehouseHandler(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    @Transactional
    public RegisterWarehouseResult execute(RegisterWarehouseCommand input) {
        warehouseRepository.findByCode(input.code().trim().toUpperCase())
                .ifPresent(w -> {
                    throw new DuplicateWarehouseCodeException(input.code());
                });

        Long id = warehouseRepository.nextIdentity();
        Warehouse warehouse = Warehouse.create(
                id,
                input.organizationId(),
                input.name(),
                input.code(),
                input.facilityType(),
                input.address(),
                input.managerId(),
                input.correlationId()
        );

        warehouseRepository.save(warehouse);

        return new RegisterWarehouseResult(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getCode(),
                warehouse.getFacilityType().name(),
                warehouse.getStatus().name()
        );
    }
}
