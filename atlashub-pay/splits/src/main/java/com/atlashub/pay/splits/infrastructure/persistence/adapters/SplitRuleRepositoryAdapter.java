package com.atlashub.pay.splits.infrastructure.persistence.adapters;

import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.pay.splits.domain.repositories.SplitRuleRepository;
import com.atlashub.pay.splits.infrastructure.persistence.entities.SplitRuleJpa;
import com.atlashub.pay.splits.infrastructure.persistence.mappers.SplitRuleMapper;
import com.atlashub.pay.splits.infrastructure.persistence.repositories.SpringDataSplitRuleRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SplitRuleRepositoryAdapter extends JpaBaseRepository<SplitRule, SplitRuleJpa> implements SplitRuleRepository {

    private final SpringDataSplitRuleRepository springDataRepo;

    public SplitRuleRepositoryAdapter(SpringDataSplitRuleRepository springDataRepo,
                                      SplitRuleMapper mapper,
                                      DomainSequenceGenerator sequenceGenerator,
                                      DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "split_rule_id_seq";
    }

    @Override
    public List<SplitRule> findAllByOrganizationId(Long organizationId) {
        return springDataRepo.findAllByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<SplitRule> findByIdAndOrganizationId(Long id, Long organizationId) {
        return springDataRepo.findByIdAndOrganizationId(id, organizationId)
                .map(mapper::toDomain);
    }
}
