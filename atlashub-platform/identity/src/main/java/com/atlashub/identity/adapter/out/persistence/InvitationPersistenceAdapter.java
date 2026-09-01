package com.atlashub.identity.adapter.out.persistence;

import com.atlashub.identity.adapter.out.persistence.mapper.InvitationMapper;
import com.atlashub.identity.adapter.out.persistence.repository.SpringDataInvitationRepository;
import com.atlashub.identity.domain.model.Invitation;
import com.atlashub.identity.domain.repository.InvitationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class InvitationPersistenceAdapter implements InvitationRepository {

    private final SpringDataInvitationRepository repository;
    private final InvitationMapper mapper;

    public InvitationPersistenceAdapter(SpringDataInvitationRepository repository, InvitationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Invitation save(Invitation invitation) {
        return mapper.toDomain(repository.save(mapper.toEntity(invitation)));
    }

    @Override
    public Optional<Invitation> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Invitation> findByToken(String token) {
        return repository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public List<Invitation> findByOrganizationId(Long organizationId) {
        return repository.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invitation> findByInvitedEmail(String email) {
        return repository.findByInvitedEmail(email).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invitation> findExpiredPendingInvitations() {
        return repository.findExpiredPendingInvitations().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
