package com.atlashub.notifications.adapter.in.messaging;

import com.atlashub.shared.application.port.out.EventTrackerPort;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class NotificationSubscriptionRegistrar implements ApplicationRunner {

    private final EventTrackerPort EventTrackerPort;

    public NotificationSubscriptionRegistrar(EventTrackerPort EventTrackerPort) {
        this.EventTrackerPort = EventTrackerPort;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Auth events
        EventTrackerPort.registerSubscription("VerificationCreated", "notifications-auth-group");
        EventTrackerPort.registerSubscription("PasswordSetupInitiated", "notifications-auth-group");
        EventTrackerPort.registerSubscription("AuthNewDeviceLoginEvent", "notifications-auth-group");
        EventTrackerPort.registerSubscription("AdminCredentialsCreated", "notifications-admin-creds-group");
        
        // Identity events
        EventTrackerPort.registerSubscription("OrganizationMemberAdded", "notifications-identity-group");
        
        // Account events
        EventTrackerPort.registerSubscription("VirtualAccountActivatedEvent", "notifications-account-group");

        // Webhook events
        EventTrackerPort.registerSubscription("WebhookDeliveryRequested", "notifications-webhook-group");
    }
}
