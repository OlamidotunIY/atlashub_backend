package com.atlashub.pay.controls.infrastructure.persistence.adapters;

import com.atlashub.pay.controls.domain.entities.ControlCase;
import com.atlashub.pay.controls.domain.repositories.ControlCaseRepository;
import com.atlashub.pay.controls.infrastructure.persistence.entities.ControlCaseJpa;
import com.atlashub.pay.controls.infrastructure.persistence.mappers.ControlCaseMapper;
import com.atlashub.pay.controls.infrastructure.persistence.repositories.SpringDataControlCaseRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ControlCaseRepositoryAdapter
        extends JpaBaseRepository<ControlCase, ControlCaseJpa>
        implements ControlCaseRepository {

    private final SpringDataControlCaseRepository springDataRepo;

    public ControlCaseRepositoryAdapter(
            SpringDataControlCaseRepository springDataRepo,
            ControlCaseMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "pay_control_case_seq";
    }

    @Override
    public Optional<ControlCase> findByDecisionId(Long decisionId) {
        return springDataRepo.findByDecisionId(decisionId)
                .map(mapper::toDomain);
    }

    @Override
    public PageResult<ControlCase> findAllByOrganizationId(Long organizationId, int pageNumber, int pageSize) {
        Page<ControlCaseJpa> page = springDataRepo.findAllByOrganizationId(
                organizationId,
                PageRequest.of(pageNumber, pageSize)
        );

        List<ControlCase> content = page.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        return new PageResult<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
