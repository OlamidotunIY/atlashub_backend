package com.atlashub.authentication.infrastructure.persistence.adapters;

import com.atlashub.authentication.domain.entities.TrustedDevice;
import com.atlashub.authentication.domain.repositories.TrustedDeviceRepository;
import com.atlashub.authentication.infrastructure.persistence.entities.TrustedDeviceJpa;
import com.atlashub.authentication.infrastructure.persistence.mappers.TrustedDeviceMapper;
import com.atlashub.authentication.infrastructure.persistence.repositories.SpringDataTrustedDeviceRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TrustedDeviceRepositoryAdapter extends JpaBaseRepository<TrustedDevice, TrustedDeviceJpa> implements TrustedDeviceRepository {

    private final SpringDataTrustedDeviceRepository springDataRepo;

    public TrustedDeviceRepositoryAdapter(SpringDataTrustedDeviceRepository springDataRepo, TrustedDeviceMapper mapper,
                                          DomainSequenceGenerator sequenceGenerator, DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "trusted_device_seq";
    }

    @Override
    public Optional<TrustedDevice> findByUserIdAndDeviceFingerprint(Long userId, String fingerPrint) {
        return springDataRepo.findByUserIdAndDeviceFingerprint(userId, fingerPrint).map(mapper::toDomain);
    }

    @Override
    public List<TrustedDevice> findAllByUserId(Long userId) {
        return springDataRepo.findAllByUserId(userId).stream().map(mapper::toDomain).toList();
    }
}
