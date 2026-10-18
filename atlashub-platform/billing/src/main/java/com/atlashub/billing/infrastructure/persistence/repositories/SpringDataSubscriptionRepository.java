package com.atlashub.billing.infrastructure.persistence.repositories;

import com.atlashub.billing.domain.valueobject.SubscriptionStatus;
import com.atlashub.billing.infrastructure.persistence.entities.SubscriptionJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataSubscriptionRepository extends JpaRepository<SubscriptionJpa, Long> {

    Optional<SubscriptionJpa> findByOrganizationId(Long organizationId);

    List<SubscriptionJpa> findByStatus(SubscriptionStatus status);

    @Query("SELECT s FROM SubscriptionJpa s WHERE s.status = 'ACTIVE' AND s.nextBillingDate <= :date")
    List<SubscriptionJpa> findActiveSubscriptionsDueForRenewal(@Param("date") LocalDate date);
}
