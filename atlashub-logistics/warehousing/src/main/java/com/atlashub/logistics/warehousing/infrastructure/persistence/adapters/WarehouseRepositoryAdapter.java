package com.atlashub.logistics.warehousing.infrastructure.persistence.adapters;

import com.atlashub.logistics.warehousing.domain.entities.Warehouse;
import com.atlashub.logistics.warehousing.domain.repositories.WarehouseRepository;
import com.atlashub.logistics.warehousing.infrastructure.persistence.entities.WarehouseJpa;
import com.atlashub.logistics.warehousing.infrastructure.persistence.mappers.WarehouseMapper;
import com.atlashub.logistics.warehousing.infrastructure.persistence.repositories.SpringDataWarehouseRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class WarehouseRepositoryAdapter
        extends JpaBaseRepository<Warehouse, WarehouseJpa>
        implements WarehouseRepository {

    private final SpringDataWarehouseRepository springDataRepo;

    public WarehouseRepositoryAdapter(
            SpringDataWarehouseRepository springDataRepo,
            WarehouseMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "warehousing_warehouse_seq";
    }

    @Override
    public Optional<Warehouse> findByCode(String code) {
        return springDataRepo.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public Optional<Warehouse> findByOrganizationIdAndCode(Long organizationId, String code) {
        return springDataRepo.findByOrganizationIdAndCode(organizationId, code).map(mapper::toDomain);
    }

    @Override
    public List<Warehouse> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
