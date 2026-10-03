package com.atlashub.pay.accounts.infrastructure.persistence.adapters;

import com.atlashub.pay.accounts.domain.entities.ReservedAccount;
import com.atlashub.pay.accounts.domain.repositories.ReservedAccountRepository;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.domain.valueobject.ReservedAccountOwnerType;
import com.atlashub.pay.accounts.infrastructure.persistence.entities.ReservedAccountJpa;
import com.atlashub.pay.accounts.infrastructure.persistence.mappers.ReservedAccountMapper;
import com.atlashub.pay.accounts.infrastructure.persistence.repositories.SpringDataReservedAccountRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;
import com.atlashub.shared.application.security.ApiEnvironment;

@Component
public class ReservedAccountRepositoryAdapter extends JpaBaseRepository<ReservedAccount, ReservedAccountJpa>
        implements ReservedAccountRepository {
    private final SpringDataReservedAccountRepository repository;

    public ReservedAccountRepositoryAdapter(SpringDataReservedAccountRepository repository,
            ReservedAccountMapper mapper, DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher) {
        super(repository, mapper, sequenceGenerator, eventPublisher);
        this.repository = repository;
    }

    @Override protected String getSequenceName() { return "reserved_account_seq"; }
    @Override public Optional<ReservedAccount> findByOrganizationIdAndEnvironmentAndId(Long organizationId, ApiEnvironment environment, Long id) {
        return repository.findByOrganizationIdAndEnvironmentAndId(organizationId, environment, id).map(mapper::toDomain);
    }
    @Override public Optional<ReservedAccount> findByRequestReferenceAndEnvironment(String requestReference, ApiEnvironment environment) {
        return repository.findByRequestReferenceAndEnvironment(requestReference, environment).map(mapper::toDomain);
    }
    @Override public Optional<ReservedAccount> findByAnchorReservedAccountIdAndEnvironment(String anchorReservedAccountId, ApiEnvironment environment) {
        return repository.findByAnchorReservedAccountIdAndEnvironment(anchorReservedAccountId, environment).map(mapper::toDomain);
    }
    @Override public Optional<ReservedAccount> findActiveByOwner(Long organizationId, ApiEnvironment environment,
            ReservedAccountOwnerType ownerType, String ownerReferenceId, String provider) {
        return repository.findFirstByOrganizationIdAndEnvironmentAndOwnerTypeAndOwnerReferenceIdAndProviderAndStatusNot(
                        organizationId, environment, ownerType, ownerReferenceId, provider, ExternalAccountStatus.CLOSED)
                .map(mapper::toDomain);
    }
    @Override public Page<ReservedAccount> search(Long organizationId, ApiEnvironment environment, ReservedAccountOwnerType ownerType,
            String ownerReferenceId, ExternalAccountStatus status, Pageable pageable) {
        Specification<ReservedAccountJpa> specification = (root, query, builder) ->
                builder.equal(root.get("organizationId"), organizationId);
        specification = specification.and((root, query, builder) ->
                builder.equal(root.get("environment"), environment));
        if (ownerType != null) specification = specification.and((root, query, builder) ->
                builder.equal(root.get("ownerType"), ownerType));
        if (ownerReferenceId != null) specification = specification.and((root, query, builder) ->
                builder.equal(root.get("ownerReferenceId"), ownerReferenceId));
        if (status != null) specification = specification.and((root, query, builder) ->
                builder.equal(root.get("status"), status));
        return repository.findAll(specification, pageable).map(mapper::toDomain);
    }
}
