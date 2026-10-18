package com.atlashub.logistics.warehousing.infrastructure.persistence.adapters;

import com.atlashub.logistics.warehousing.domain.entities.WarehouseInventory;
import com.atlashub.logistics.warehousing.domain.repositories.WarehouseInventoryRepository;
import com.atlashub.logistics.warehousing.infrastructure.persistence.entities.WarehouseInventoryJpa;
import com.atlashub.logistics.warehousing.infrastructure.persistence.mappers.WarehouseInventoryMapper;
import com.atlashub.logistics.warehousing.infrastructure.persistence.repositories.SpringDataWarehouseInventoryRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class WarehouseInventoryRepositoryAdapter
        extends JpaBaseRepository<WarehouseInventory, WarehouseInventoryJpa>
        implements WarehouseInventoryRepository {

    private final SpringDataWarehouseInventoryRepository springDataRepo;

    public WarehouseInventoryRepositoryAdapter(
            SpringDataWarehouseInventoryRepository springDataRepo,
            WarehouseInventoryMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "warehousing_inventory_seq";
    }

    @Override
    public Optional<WarehouseInventory> findByWarehouseIdAndProductId(Long warehouseId, Long productId) {
        return springDataRepo.findByWarehouseIdAndProductId(warehouseId, productId).map(mapper::toDomain);
    }

    @Override
    public List<WarehouseInventory> findByWarehouseId(Long warehouseId) {
        return springDataRepo.findByWarehouseId(warehouseId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<WarehouseInventory> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
