package com.atlashub.support.infrastructure.persistence.adapters;

import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import com.atlashub.support.domain.entities.TicketSlaConfig;
import com.atlashub.support.domain.repositories.TicketSlaConfigRepository;
import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import com.atlashub.support.infrastructure.persistence.entities.TicketSlaConfigJpa;
import com.atlashub.support.infrastructure.persistence.mappers.TicketSlaConfigMapper;
import com.atlashub.support.infrastructure.persistence.repositories.SpringDataTicketSlaConfigRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TicketSlaConfigRepositoryAdapter implements TicketSlaConfigRepository {

    private final SpringDataTicketSlaConfigRepository springDataRepo;
    private final TicketSlaConfigMapper mapper;
    private final DomainSequenceGenerator sequenceGenerator;

    public TicketSlaConfigRepositoryAdapter(
            SpringDataTicketSlaConfigRepository springDataRepo,
            TicketSlaConfigMapper mapper,
            DomainSequenceGenerator sequenceGenerator
    ) {
        this.springDataRepo = springDataRepo;
        this.mapper = mapper;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("support_sla_config_seq");
    }

    @Override
    public TicketSlaConfig save(TicketSlaConfig entity) {
        TicketSlaConfigJpa jpa = mapper.toPersistence(entity);
        TicketSlaConfigJpa saved = springDataRepo.save(jpa);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<TicketSlaConfig> findById(Long id) {
        return springDataRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataRepo.existsById(id);
    }

    @Override
    public List<TicketSlaConfig> findAll() {
        return springDataRepo.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<TicketSlaConfig> findByCategoryAndPriority(TicketCategory category, TicketPriority priority) {
        return springDataRepo.findByCategoryAndPriority(category, priority).map(mapper::toDomain);
    }
}
