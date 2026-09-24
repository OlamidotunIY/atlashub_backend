package com.atlashub.authentication.infrastructure.persistence.adapters;

import com.atlashub.authentication.domain.entities.Verification;
import com.atlashub.authentication.domain.repositories.VerificationRepository;
import com.atlashub.authentication.domain.valueobject.VerificationStatus;
import com.atlashub.authentication.domain.valueobject.VerificationType;
import com.atlashub.authentication.infrastructure.persistence.entities.VerificationJpa;
import com.atlashub.authentication.infrastructure.persistence.mappers.VerificationMapper;
import com.atlashub.authentication.infrastructure.persistence.repositories.SpringDataVerificationRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VerificationRepositoryAdapter
        extends JpaBaseRepository<Verification, VerificationJpa>
        implements VerificationRepository {

    private final SpringDataVerificationRepository springDataRepo;

    public VerificationRepositoryAdapter(SpringDataVerificationRepository springDataRepo,
                                         VerificationMapper mapper,
                                         DomainSequenceGenerator sequenceGenerator,
                                         DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "verification_seq";
    }

    @Override
    public Optional<Verification> findByIdentifierAndTypeAndStatus(
            String identifier, VerificationType type, VerificationStatus status) {
        return springDataRepo
                .findByIdentifierAndVerificationTypeAndVerificationStatus(identifier, type, status)
                .map(mapper::toDomain);
    }
}
