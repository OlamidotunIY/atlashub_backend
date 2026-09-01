package com.atlashub.auth.adapter.out.persistence;

import com.atlashub.auth.domain.model.Verification;
import com.atlashub.auth.domain.valueobject.VerificationStatus;
import com.atlashub.auth.domain.valueobject.VerificationType;
import com.atlashub.auth.domain.repository.VerificationRepository;
import com.atlashub.auth.adapter.out.persistence.entity.VerificationJpaEntity;
import com.atlashub.auth.adapter.out.persistence.mapper.VerificationMapper;
import com.atlashub.auth.adapter.out.persistence.repository.SpringDataVerificationRepository;
import com.atlashub.shared.adapter.out.external.DomainSequenceGenerator;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class VerificationRepositoryAdapter implements VerificationRepository {

    private final SpringDataVerificationRepository jpaRepository;
    private final DomainSequenceGenerator sequenceGenerator;
    private final VerificationMapper mapper;

    public VerificationRepositoryAdapter(SpringDataVerificationRepository jpaRepository, DomainSequenceGenerator sequenceGenerator, VerificationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.mapper = mapper;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("verification_seq");
    }

    @Override
    public Verification save(Verification verification) {
        VerificationJpaEntity entity = mapper.toEntity(verification);
        jpaRepository.save(entity);
        return verification;
    }

    @Override
    public Optional<Verification> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Verification> findActiveByTypeAndValue(VerificationType type, String value) {
        return jpaRepository.findByTypeAndValueAndStatus(type, value, VerificationStatus.PENDING)
                .map(mapper::toDomain);
    }

    @Override
    public List<Verification> findByStatus(VerificationStatus status) {
        return jpaRepository.findByStatus(status)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void invalidatePreviousVerifications(VerificationType type, String value) {
        jpaRepository.invalidatePreviousVerifications(type, value);
    }
}
