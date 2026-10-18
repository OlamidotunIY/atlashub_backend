package com.atlashub.shared.application.port;

public interface EntitlementQueryPort {

    boolean hasActiveSubscription(Long orgId);

    SubscriptionStatus getSubscriptionStatus(Long orgId);

    boolean isSubscriptionSuspended(Long orgId);

    enum SubscriptionStatus {
        PENDING_PAYMENT,
        PENDING_COMPLIANCE,
        ACTIVE,
        SUSPENDED,
        CANCELLED,
        EXPIRED
    }
}
