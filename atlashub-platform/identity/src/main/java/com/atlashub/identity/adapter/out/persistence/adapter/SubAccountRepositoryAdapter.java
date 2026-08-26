package com.atlashub.identity.adapter.out.persistence.adapter;

import com.atlashub.identity.domain.model.SubAccount;
import com.atlashub.identity.domain.repository.SubAccountRepository;
import com.atlashub.identity.adapter.out.persistence.entity.SubAccountJpaEntity;
import com.atlashub.identity.adapter.out.persistence.mapper.SubAccountMapper;
import com.atlashub.identity.adapter.out.persistence.repository.SpringDataSubAccountRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SubAccountRepositoryAdapter implements SubAccountRepository {

    private final SpringDataSubAccountRepository jpaRepository;
    private final com.atlashub.shared.adapter.out.external.DomainSequenceGenerator sequenceGenerator;
    private final SubAccountMapper mapper;

    public SubAccountRepositoryAdapter(SpringDataSubAccountRepository jpaRepository, com.atlashub.shared.adapter.out.external.DomainSequenceGenerator sequenceGenerator, SubAccountMapper mapper) {
        this.sequenceGenerator = sequenceGenerator;
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public SubAccount save(SubAccount subAccount) {
        SubAccountJpaEntity entity = mapper.toEntity(subAccount);
        jpaRepository.save(entity);
        return subAccount;
    }

    @Override
    public Optional<SubAccount> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<SubAccount> findByMerchantIdAndBankCodeAndAccountNumber(Long merchantId, String bankCode, String accountNumber) {
        return jpaRepository.findByIntegrationAndBankCodeAndAccountNumber(merchantId, bankCode, accountNumber).map(mapper::toDomain);
    }


    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("subaccount_seq");
    }


}
