package com.atlashub.audit.adapter.in.messaging;

import com.atlashub.shared.application.api.EventTrackerApi;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AuditSubscriptionRegistrar implements ApplicationRunner {

    private final EventTrackerApi eventTrackerApi;

    public AuditSubscriptionRegistrar(EventTrackerApi eventTrackerApi) {
        this.eventTrackerApi = eventTrackerApi;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Auth events
        eventTrackerApi.registerSubscription("AuthAccountCreated", "audit-group");
        
        // Identity events
        eventTrackerApi.registerSubscription("UserCreated", "audit-group");
        eventTrackerApi.registerSubscription("OrganizationCreated", "audit-group");
        eventTrackerApi.registerSubscription("OrganizationMemberAdded", "audit-group");
        eventTrackerApi.registerSubscription("OrganizationMemberRemoved", "audit-group");
        eventTrackerApi.registerSubscription("InvitationCreated", "audit-group");
        eventTrackerApi.registerSubscription("InvitationAccepted", "audit-group");
        eventTrackerApi.registerSubscription("ApiKeyCreated", "audit-group");
    }
}
