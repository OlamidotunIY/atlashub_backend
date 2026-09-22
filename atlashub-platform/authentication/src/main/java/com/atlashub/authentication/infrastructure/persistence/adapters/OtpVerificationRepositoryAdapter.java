package com.atlashub.authentication.infrastructure.persistence.adapters;

import com.atlashub.authentication.domain.entities.OtpVerification;
import com.atlashub.authentication.domain.repositories.OtpVerificationRepository;
import com.atlashub.authentication.domain.valueobject.OtpStatus;
import com.atlashub.authentication.domain.valueobject.OtpType;
import com.atlashub.authentication.infrastructure.persistence.entities.OtpVerificationJpa;
import com.atlashub.authentication.infrastructure.persistence.mappers.OtpVerificationMapper;
import com.atlashub.authentication.infrastructure.persistence.repositories.SpringDataOtpVerificationRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OtpVerificationRepositoryAdapter extends JpaBaseRepository<OtpVerification, OtpVerificationJpa> implements OtpVerificationRepository {

    private final SpringDataOtpVerificationRepository springDataRepo;

    public OtpVerificationRepositoryAdapter(SpringDataOtpVerificationRepository springDataRepo, OtpVerificationMapper mapper,
                                            DomainSequenceGenerator sequenceGenerator, DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "otp_verification_seq";
    }

    @Override
    public Optional<OtpVerification> findByAuthAccountIdAndTypeAndStatus(Long authAccountId, OtpType type, OtpStatus status) {
        return springDataRepo.findByAuthAccountIdAndTypeAndStatus(authAccountId, type, status)
                .map(mapper::toDomain);
    }
}
