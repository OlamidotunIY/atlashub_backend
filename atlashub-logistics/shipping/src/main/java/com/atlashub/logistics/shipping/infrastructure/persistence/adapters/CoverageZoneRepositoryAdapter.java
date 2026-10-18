package com.atlashub.logistics.shipping.infrastructure.persistence.adapters;

import com.atlashub.logistics.shipping.domain.entities.CoverageZone;
import com.atlashub.logistics.shipping.domain.repositories.CoverageZoneRepository;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.CoverageZoneJpa;
import com.atlashub.logistics.shipping.infrastructure.persistence.mappers.CoverageZoneMapper;
import com.atlashub.logistics.shipping.infrastructure.persistence.repositories.SpringDataCoverageZoneRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CoverageZoneRepositoryAdapter
        extends JpaBaseRepository<CoverageZone, CoverageZoneJpa>
        implements CoverageZoneRepository {

    private final SpringDataCoverageZoneRepository springDataRepo;

    public CoverageZoneRepositoryAdapter(
            SpringDataCoverageZoneRepository springDataRepo,
            CoverageZoneMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "logistics_zone_seq";
    }

    @Override
    public Optional<CoverageZone> findByZoneCode(String zoneCode) {
        return springDataRepo.findByZoneCode(zoneCode).map(mapper::toDomain);
    }

    @Override
    public Optional<CoverageZone> findByStateAndCity(String state, String city) {
        return springDataRepo.findByStateIgnoreCaseAndCityIgnoreCase(state, city).map(mapper::toDomain);
    }

    @Override
    public List<CoverageZone> findAllActive() {
        return springDataRepo.findByActiveTrue().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
