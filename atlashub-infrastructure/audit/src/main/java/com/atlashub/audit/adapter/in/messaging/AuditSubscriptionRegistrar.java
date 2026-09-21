package com.atlashub.audit.adapter.in.messaging;

import com.atlashub.shared.application.port.EventTrackerPort;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AuditSubscriptionRegistrar implements ApplicationRunner {

    private final EventTrackerPort EventTrackerPort;

    public AuditSubscriptionRegistrar(EventTrackerPort EventTrackerPort) {
        this.EventTrackerPort = EventTrackerPort;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Auth events
        EventTrackerPort.registerSubscription("AuthAccountCreated", "audit-group");
        
        // Identity events
        EventTrackerPort.registerSubscription("UserCreated", "audit-group");
        EventTrackerPort.registerSubscription("OrganizationCreated", "audit-group");
        EventTrackerPort.registerSubscription("OrganizationMemberAdded", "audit-group");
        EventTrackerPort.registerSubscription("OrganizationMemberRemoved", "audit-group");
        EventTrackerPort.registerSubscription("InvitationCreated", "audit-group");
        EventTrackerPort.registerSubscription("InvitationAccepted", "audit-group");
        EventTrackerPort.registerSubscription("ApiKeyCreated", "audit-group");
    }
}
