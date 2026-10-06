package com.atlashub.accounts.infrastructure.persistence.adapters;

import com.atlashub.accounts.domain.entities.Outlet;
import com.atlashub.accounts.domain.repositories.OutletRepository;
import com.atlashub.accounts.infrastructure.persistence.entities.OutletJpa;
import com.atlashub.accounts.infrastructure.persistence.mappers.OutletMapper;
import com.atlashub.accounts.infrastructure.persistence.repositories.SpringDataOutletRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutletRepositoryAdapter extends JpaBaseRepository<Outlet, OutletJpa>
        implements OutletRepository {

    private final SpringDataOutletRepository springDataRepo;

    public OutletRepositoryAdapter(SpringDataOutletRepository springDataRepo,
                                   OutletMapper mapper,
                                   DomainSequenceGenerator sequenceGenerator,
                                   DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "outlet_seq";
    }

    @Override
    public List<Outlet> findAllByOrganizationId(Long organizationId) {
        return springDataRepo.findAllByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
