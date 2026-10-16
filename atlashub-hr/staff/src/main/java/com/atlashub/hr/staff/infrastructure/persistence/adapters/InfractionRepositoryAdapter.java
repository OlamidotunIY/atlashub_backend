package com.atlashub.hr.staff.infrastructure.persistence.adapters;

import com.atlashub.hr.staff.domain.entities.Infraction;
import com.atlashub.hr.staff.domain.repositories.InfractionRepository;
import com.atlashub.hr.staff.infrastructure.persistence.entities.InfractionJpaEntity;
import com.atlashub.hr.staff.infrastructure.persistence.mappers.InfractionMapper;
import com.atlashub.hr.staff.infrastructure.persistence.repositories.SpringDataInfractionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InfractionRepositoryAdapter
        extends JpaBaseRepository<Infraction, InfractionJpaEntity>
        implements InfractionRepository {

    private final SpringDataInfractionRepository springDataRepo;

    public InfractionRepositoryAdapter(
            SpringDataInfractionRepository springDataRepo,
            InfractionMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "hr_infraction_seq";
    }

    @Override
    public List<Infraction> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Infraction> findByEmployeeId(Long employeeId) {
        return springDataRepo.findByEmployeeId(employeeId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
