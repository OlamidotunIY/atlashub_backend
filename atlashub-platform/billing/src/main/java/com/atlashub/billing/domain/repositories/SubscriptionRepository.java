package com.atlashub.billing.domain.repositories;

import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.valueobject.SubscriptionStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends Repository<Subscription> {

    Optional<Subscription> findByOrganizationId(Long organizationId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    List<Subscription> findActiveSubscriptionsDueForRenewal(LocalDate date);
}
