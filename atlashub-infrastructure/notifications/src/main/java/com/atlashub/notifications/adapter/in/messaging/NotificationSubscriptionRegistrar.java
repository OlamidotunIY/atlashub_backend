package com.atlashub.notifications.adapter.in.messaging;

import com.atlashub.shared.application.api.EventTrackerApi;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class NotificationSubscriptionRegistrar implements ApplicationRunner {

    private final EventTrackerApi eventTrackerApi;

    public NotificationSubscriptionRegistrar(EventTrackerApi eventTrackerApi) {
        this.eventTrackerApi = eventTrackerApi;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Auth events
        eventTrackerApi.registerSubscription("VerificationCreated", "notifications-auth-group");
        eventTrackerApi.registerSubscription("PasswordSetupInitiated", "notifications-auth-group");
        eventTrackerApi.registerSubscription("AuthNewDeviceLoginEvent", "notifications-auth-group");
        eventTrackerApi.registerSubscription("AdminCredentialsCreated", "notifications-admin-creds-group");
        
        // Identity events
        eventTrackerApi.registerSubscription("OrganizationMemberAdded", "notifications-identity-group");
        
        // Account events
        eventTrackerApi.registerSubscription("VirtualAccountActivatedEvent", "notifications-account-group");

        // Webhook events
        eventTrackerApi.registerSubscription("WebhookDeliveryRequested", "notifications-webhook-group");
    }
}
