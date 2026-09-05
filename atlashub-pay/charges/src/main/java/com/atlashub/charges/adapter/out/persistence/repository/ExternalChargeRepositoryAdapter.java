package com.atlashub.charges.adapter.out.persistence.repository;

import com.atlashub.charges.adapter.out.persistence.mapper.ExternalChargeMapper;
import com.atlashub.charges.domain.model.ExternalCharge;
import com.atlashub.charges.domain.repository.ExternalChargeRepository;
import com.atlashub.shared.adapter.out.external.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ExternalChargeRepositoryAdapter implements ExternalChargeRepository {

    private final SpringDataExternalChargeRepository jpaRepository;
    private final ExternalChargeMapper mapper;
    private final DomainSequenceGenerator sequenceGenerator;

    public ExternalChargeRepositoryAdapter(
            SpringDataExternalChargeRepository jpaRepository,
            ExternalChargeMapper mapper,
            DomainSequenceGenerator sequenceGenerator) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("paystack_charge_seq");
    }

    @Override
    public ExternalCharge save(ExternalCharge charge) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(charge)));
    }

    @Override
    public Optional<ExternalCharge> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public boolean existsById(Long id) {
        return false;
    }

    @Override
    public Optional<ExternalCharge> findByReference(String reference) {
        return jpaRepository.findByReference(reference).map(mapper::toDomain);
    }
}
