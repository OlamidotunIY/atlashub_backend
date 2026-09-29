package com.atlashub.iam.infrastructure.persistence.adapters;

import com.atlashub.iam.domain.entities.ApiKey;
import com.atlashub.iam.domain.repositories.ApiKeyRepository;
import com.atlashub.iam.infrastructure.persistence.repositories.SpringDataApiKeyRepository;
import com.atlashub.iam.domain.valueobject.ApiEnvironment;
import com.atlashub.iam.infrastructure.persistence.entities.ApiKeyJpa;
import com.atlashub.iam.infrastructure.persistence.mappers.ApiKeyMapper;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiKeyRepositoryAdapter
        extends JpaBaseRepository<ApiKey, ApiKeyJpa>
        implements ApiKeyRepository {

    private final SpringDataApiKeyRepository springDataRepo;

    public ApiKeyRepositoryAdapter(SpringDataApiKeyRepository springDataRepo,
                                   ApiKeyMapper mapper,
                                   DomainSequenceGenerator sequenceGenerator,
                                   DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "api_key_seq";
    }

    @Override
    public List<ApiKey> findByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment) {
        return springDataRepo.findByOrganizationIdAndEnvironment(organizationId, environment)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}


