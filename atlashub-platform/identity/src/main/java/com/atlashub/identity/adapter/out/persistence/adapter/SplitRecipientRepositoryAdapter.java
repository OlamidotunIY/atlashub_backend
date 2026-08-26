package com.atlashub.identity.adapter.out.persistence.adapter;

import com.atlashub.identity.domain.model.SplitRecipient;
import com.atlashub.identity.domain.repository.SplitRecipientRepository;
import com.atlashub.identity.adapter.out.persistence.entity.SplitRecipientJpaEntity;
import com.atlashub.identity.adapter.out.persistence.mapper.SplitRecipientMapper;
import com.atlashub.identity.adapter.out.persistence.repository.SpringDataSplitRecipientRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SplitRecipientRepositoryAdapter implements SplitRecipientRepository {

    private final SpringDataSplitRecipientRepository jpaRepository;
    private final com.atlashub.shared.adapter.out.external.DomainSequenceGenerator sequenceGenerator;
    private final SplitRecipientMapper mapper;

    public SplitRecipientRepositoryAdapter(SpringDataSplitRecipientRepository jpaRepository, com.atlashub.shared.adapter.out.external.DomainSequenceGenerator sequenceGenerator, SplitRecipientMapper mapper) {
        this.sequenceGenerator = sequenceGenerator;
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public SplitRecipient save(SplitRecipient SplitRecipient) {
        SplitRecipientJpaEntity entity = mapper.toEntity(SplitRecipient);
        jpaRepository.save(entity);
        return SplitRecipient;
    }

    @Override
    public Optional<SplitRecipient> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<SplitRecipient> findByOrganizationIdAndBankCodeAndAccountNumber(Long OrganizationId, String bankCode, String accountNumber) {
        return jpaRepository.findByIntegrationAndBankCodeAndAccountNumber(OrganizationId, bankCode, accountNumber).map(mapper::toDomain);
    }


    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("SplitRecipient_seq");
    }


}
