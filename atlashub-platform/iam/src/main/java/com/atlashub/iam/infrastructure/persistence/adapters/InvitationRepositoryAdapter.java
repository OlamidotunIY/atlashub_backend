package com.atlashub.iam.infrastructure.persistence.adapters;

import com.atlashub.iam.domain.entities.Invitation;
import com.atlashub.iam.domain.repositories.InvitationRepository;
import com.atlashub.iam.domain.valueobject.InvitationStatus;
import com.atlashub.iam.infrastructure.persistence.entities.InvitationJpa;
import com.atlashub.iam.infrastructure.persistence.mappers.InvitationMapper;
import com.atlashub.iam.infrastructure.persistence.repositories.SpringDataInvitationRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InvitationRepositoryAdapter 
        extends JpaBaseRepository<Invitation, InvitationJpa> 
        implements InvitationRepository {

    private final SpringDataInvitationRepository springDataRepo;

    public InvitationRepositoryAdapter(SpringDataInvitationRepository springDataRepo,
                                       InvitationMapper mapper,
                                       DomainSequenceGenerator sequenceGenerator,
                                       DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "invitation_seq";
    }

    @Override
    public Optional<Invitation> findByToken(String token) {
        return springDataRepo.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public List<Invitation> findByOrganizationIdAndStatus(Long organizationId, InvitationStatus status) {
        return springDataRepo.findByOrganizationIdAndStatus(organizationId, status)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}

