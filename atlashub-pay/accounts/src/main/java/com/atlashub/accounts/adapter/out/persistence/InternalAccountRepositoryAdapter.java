package com.atlashub.accounts.adapter.out.persistence;

import com.atlashub.accounts.adapter.out.persistence.entity.InternalAccountEntity;
import com.atlashub.accounts.adapter.out.persistence.mapper.InternalAccountMapper;
import com.atlashub.accounts.adapter.out.persistence.repository.SpringDataInternalAccountRepository;
import com.atlashub.accounts.domain.model.InternalAccount;
import com.atlashub.accounts.domain.repository.InternalAccountDomainRepository;
import com.atlashub.accounts.domain.valueobject.InternalAccountType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class InternalAccountRepositoryAdapter implements InternalAccountDomainRepository {

    private final SpringDataInternalAccountRepository springDataRepository;
    private final InternalAccountMapper mapper;
    private final com.atlashub.shared.adapter.out.external.DomainSequenceGenerator idGenerator;

    public InternalAccountRepositoryAdapter(
            SpringDataInternalAccountRepository springDataRepository,
            InternalAccountMapper mapper,
            com.atlashub.shared.adapter.out.external.DomainSequenceGenerator idGenerator) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public Long nextIdentity() {
        return idGenerator.nextIdentity("internal_account_seq");
    }

    @Override
    public InternalAccount save(InternalAccount account) {
        InternalAccountEntity entity = mapper.toEntity(account);
        InternalAccountEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<InternalAccount> findById(Long id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataRepository.existsById(id);
    }

    @Override
    public List<InternalAccount> findByOrganizationId(Long organizationId) {
        return springDataRepository.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<InternalAccount> findByOrganizationIdAndType(Long organizationId, InternalAccountType type) {
        return springDataRepository.findByOrganizationIdAndType(organizationId, type.name())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByOrganizationIdAndType(Long organizationId, InternalAccountType type) {
        return springDataRepository.existsByOrganizationIdAndType(organizationId, type.name());
    }
}