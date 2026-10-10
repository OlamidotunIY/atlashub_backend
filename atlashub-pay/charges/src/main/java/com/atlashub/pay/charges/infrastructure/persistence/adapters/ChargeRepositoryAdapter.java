package com.atlashub.pay.charges.infrastructure.persistence.adapters;

import com.atlashub.pay.charges.domain.entities.Charge;
import com.atlashub.pay.charges.domain.repositories.ChargeRepository;
import com.atlashub.pay.charges.infrastructure.persistence.entities.ChargeJpa;
import com.atlashub.pay.charges.infrastructure.persistence.mappers.ChargeMapper;
import com.atlashub.pay.charges.infrastructure.persistence.repositories.SpringDataChargeRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class ChargeRepositoryAdapter extends JpaBaseRepository<Charge, ChargeJpa> implements ChargeRepository {
    private final SpringDataChargeRepository repository;

    public ChargeRepositoryAdapter(SpringDataChargeRepository repository, ChargeMapper mapper,
                                   DomainSequenceGenerator sequence, DomainEventPublisher publisher) {
        super(repository, mapper, sequence, publisher);
        this.repository = repository;
    }

    @Override
    protected String getSequenceName() {
        return "pay_charge_seq";
    }

    @Override
    public Optional<Charge> findByOrganizationIdAndEnvironmentAndReference(Long org, ApiEnvironment env, String ref) {
        return repository.findByOrganizationIdAndEnvironmentAndReference(org, env, ref).map(mapper::toDomain);
    }

    @Override
    public Optional<Charge> findByProviderReferenceAndEnvironment(String ref, ApiEnvironment env) {
        return repository.findByProviderReferenceAndEnvironment(ref, env).map(mapper::toDomain);
    }

    @Override
    public List<Charge> findPendingCharges(ZonedDateTime createdBefore) {
        return repository.findPendingCharges(createdBefore).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
