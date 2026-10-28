package com.atlashub.authentication.infrastructure.persistence.adapters;

import com.atlashub.authentication.domain.entities.AuthorizationCode;
import com.atlashub.authentication.domain.repositories.AuthorizationCodeRepository;
import com.atlashub.authentication.infrastructure.persistence.entities.AuthorizationCodeJpa;
import com.atlashub.authentication.infrastructure.persistence.mappers.AuthorizationCodeMapper;
import com.atlashub.authentication.infrastructure.persistence.repositories.SpringDataAuthorizationCodeRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Optional;

@Component
public class AuthorizationCodeRepositoryAdapter
        extends JpaBaseRepository<AuthorizationCode, AuthorizationCodeJpa>
        implements AuthorizationCodeRepository {

    private final SpringDataAuthorizationCodeRepository springDataRepo;

    public AuthorizationCodeRepositoryAdapter(SpringDataAuthorizationCodeRepository springDataRepo,
                                              AuthorizationCodeMapper mapper,
                                              DomainSequenceGenerator sequenceGenerator,
                                              DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "oauth_auth_code_seq";
    }

    @Override
    public Optional<AuthorizationCode> findByCodeHash(String codeHash) {
        if (codeHash == null || codeHash.isBlank()) {
            return Optional.empty();
        }
        return springDataRepo.findByCodeHash(codeHash.trim()).map(mapper::toDomain);
    }

    @Override
    public void deleteExpiredBefore(ZonedDateTime threshold) {
        if (threshold != null) {
            springDataRepo.deleteExpiredBefore(threshold);
        }
    }
}
