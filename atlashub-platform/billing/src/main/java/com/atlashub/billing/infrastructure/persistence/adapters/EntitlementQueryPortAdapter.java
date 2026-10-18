package com.atlashub.billing.infrastructure.persistence.adapters;

import com.atlashub.billing.infrastructure.persistence.repositories.SpringDataSubscriptionRepository;
import com.atlashub.shared.application.port.EntitlementQueryPort;
import org.springframework.stereotype.Component;

@Component
public class EntitlementQueryPortAdapter implements EntitlementQueryPort {

    private final SpringDataSubscriptionRepository springDataRepo;

    public EntitlementQueryPortAdapter(SpringDataSubscriptionRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public boolean hasActiveSubscription(Long orgId) {
        if (orgId == null) {
            return false;
        }
        return springDataRepo.findByOrganizationId(orgId)
                .map(sub -> sub.getStatus() == com.atlashub.billing.domain.valueobject.SubscriptionStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    public SubscriptionStatus getSubscriptionStatus(Long orgId) {
        if (orgId == null) {
            return null;
        }
        return springDataRepo.findByOrganizationId(orgId)
                .map(sub -> SubscriptionStatus.valueOf(sub.getStatus().name()))
                .orElse(null);
    }

    @Override
    public boolean isSubscriptionSuspended(Long orgId) {
        if (orgId == null) {
            return false;
        }
        return springDataRepo.findByOrganizationId(orgId)
                .map(sub -> sub.getStatus() == com.atlashub.billing.domain.valueobject.SubscriptionStatus.SUSPENDED)
                .orElse(false);
    }
}
