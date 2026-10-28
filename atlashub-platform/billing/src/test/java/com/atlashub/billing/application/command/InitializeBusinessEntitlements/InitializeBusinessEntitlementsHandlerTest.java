package com.atlashub.billing.application.command.InitializeBusinessEntitlements;

import com.atlashub.billing.domain.entities.Subscription;
import com.atlashub.billing.domain.repositories.SubscriptionRepository;
import com.atlashub.billing.domain.valueobject.SubscriptionStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InitializeBusinessEntitlementsHandlerTest {
    @Test
    void starts_active_access_once_without_creating_an_invoice() {
        SubscriptionRepository subscriptions = mock(SubscriptionRepository.class);
        when(subscriptions.findByOrganizationId(10L)).thenReturn(Optional.empty());
        when(subscriptions.nextIdentity()).thenReturn(20L);
        InitializeBusinessEntitlementsHandler handler = new InitializeBusinessEntitlementsHandler(subscriptions);

        handler.execute(10L, "GHS");

        ArgumentCaptor<Subscription> saved = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptions).save(saved.capture());
        assertEquals(SubscriptionStatus.ACTIVE, saved.getValue().getStatus());
        assertEquals("GHS", saved.getValue().getBillingCurrency());
    }
}
