package com.atlashub.authentication.infrastructure.persistence.adapters;

import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.infrastructure.persistence.entities.AuthAccountJpa;
import com.atlashub.authentication.infrastructure.persistence.mappers.AuthAccountMapper;
import com.atlashub.authentication.infrastructure.persistence.repositories.SpringDataAuthAccountRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthAccountRepositoryAdapter extends JpaBaseRepository<AuthAccount, AuthAccountJpa> implements AuthAccountRepository {

    private final SpringDataAuthAccountRepository springDataRepo;

    public AuthAccountRepositoryAdapter(SpringDataAuthAccountRepository springDataRepo, AuthAccountMapper mapper,
                                        DomainSequenceGenerator sequenceGenerator, DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "auth_account_seq";
    }

    @Override
    public Optional<AuthAccount> findByEmail(String email) {
        return springDataRepo.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<AuthAccount> findByUserId(Long userId) {
        return springDataRepo.findByUserId(userId).map(mapper::toDomain);
    }
}
