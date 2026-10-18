package com.atlashub.billing.infrastructure.persistence.adapters;

import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.repositories.SubscriptionRepository;
import com.atlashub.billing.domain.valueobject.SubscriptionStatus;
import com.atlashub.billing.infrastructure.persistence.entities.SubscriptionJpa;
import com.atlashub.billing.infrastructure.persistence.mappers.SubscriptionMapper;
import com.atlashub.billing.infrastructure.persistence.repositories.SpringDataSubscriptionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class SubscriptionRepositoryAdapter
        extends JpaBaseRepository<Subscription, SubscriptionJpa>
        implements SubscriptionRepository {

    private final SpringDataSubscriptionRepository springDataRepo;

    public SubscriptionRepositoryAdapter(
            SpringDataSubscriptionRepository springDataRepo,
            SubscriptionMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "billing_subscription_seq";
    }

    @Override
    public Optional<Subscription> findByOrganizationId(Long organizationId) {
        if (organizationId == null) {
            return Optional.empty();
        }
        return springDataRepo.findByOrganizationId(organizationId).map(mapper::toDomain);
    }

    @Override
    public List<Subscription> findByStatus(SubscriptionStatus status) {
        if (status == null) {
            return List.of();
        }
        return springDataRepo.findByStatus(status).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Subscription> findActiveSubscriptionsDueForRenewal(LocalDate date) {
        if (date == null) {
            return List.of();
        }
        return springDataRepo.findActiveSubscriptionsDueForRenewal(date).stream().map(mapper::toDomain).toList();
    }
}
