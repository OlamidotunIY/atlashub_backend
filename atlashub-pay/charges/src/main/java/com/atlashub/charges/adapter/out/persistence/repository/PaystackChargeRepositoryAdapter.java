package com.atlashub.charges.adapter.out.persistence.repository;

import com.atlashub.charges.adapter.out.persistence.mapper.PaystackChargeMapper;
import com.atlashub.charges.domain.model.PaystackCharge;
import com.atlashub.charges.domain.repository.PaystackChargeRepository;
import com.atlashub.shared.adapter.out.external.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaystackChargeRepositoryAdapter implements PaystackChargeRepository {

    private final SpringDataPaystackChargeRepository jpaRepository;
    private final PaystackChargeMapper mapper;
    private final DomainSequenceGenerator sequenceGenerator;

    public PaystackChargeRepositoryAdapter(
            SpringDataPaystackChargeRepository jpaRepository,
            PaystackChargeMapper mapper,
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
    public PaystackCharge save(PaystackCharge charge) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(charge)));
    }

    @Override
    public Optional<PaystackCharge> findById(Long id) {
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
    public Optional<PaystackCharge> findByReference(String reference) {
        return jpaRepository.findByReference(reference).map(mapper::toDomain);
    }
}
