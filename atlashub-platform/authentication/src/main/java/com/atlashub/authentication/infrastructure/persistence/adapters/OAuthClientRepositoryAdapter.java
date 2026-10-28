package com.atlashub.authentication.infrastructure.persistence.adapters;

import com.atlashub.authentication.domain.entities.OAuthClient;
import com.atlashub.authentication.domain.repositories.OAuthClientRepository;
import com.atlashub.authentication.infrastructure.persistence.entities.OAuthClientJpa;
import com.atlashub.authentication.infrastructure.persistence.mappers.OAuthClientMapper;
import com.atlashub.authentication.infrastructure.persistence.repositories.SpringDataOAuthClientRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OAuthClientRepositoryAdapter
        extends JpaBaseRepository<OAuthClient, OAuthClientJpa>
        implements OAuthClientRepository {

    private final SpringDataOAuthClientRepository springDataRepo;

    public OAuthClientRepositoryAdapter(SpringDataOAuthClientRepository springDataRepo,
                                        OAuthClientMapper mapper,
                                        DomainSequenceGenerator sequenceGenerator,
                                        DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "oauth_client_seq";
    }

    @Override
    public Optional<OAuthClient> findByClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return Optional.empty();
        }
        return springDataRepo.findByClientId(clientId.trim()).map(mapper::toDomain);
    }
}
