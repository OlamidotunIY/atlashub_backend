package com.atlashub.billing.infrastructure.persistence.adapters;

import com.atlashub.billing.domain.valueobject.SubscriptionStatus;
import com.atlashub.billing.infrastructure.persistence.entities.SubscriptionJpa;
import com.atlashub.billing.infrastructure.persistence.repositories.SpringDataSubscriptionRepository;
import com.atlashub.shared.application.port.EntitlementQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class EntitlementQueryPortAdapterTest {

    private SpringDataSubscriptionRepository springDataRepo;
    private EntitlementQueryPortAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = Mockito.mock(SpringDataSubscriptionRepository.class);
        adapter = new EntitlementQueryPortAdapter(springDataRepo);
    }

    @Test
    void hasActiveSubscription_returnsTrue_whenActive() {
        SubscriptionJpa jpa = new SubscriptionJpa(
                1L, 50L, 1L, SubscriptionStatus.ACTIVE,
                LocalDate.now(), LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(1),
                "NGN", null, null, null, ZonedDateTime.now(), ZonedDateTime.now(), 0L
        );
        when(springDataRepo.findByOrganizationId(50L)).thenReturn(Optional.of(jpa));

        assertTrue(adapter.hasActiveSubscription(50L));
        assertEquals(EntitlementQueryPort.SubscriptionStatus.ACTIVE, adapter.getSubscriptionStatus(50L));
        assertFalse(adapter.isSubscriptionSuspended(50L));
    }

    @Test
    void hasActiveSubscription_returnsFalse_whenSuspendedOrPending() {
        SubscriptionJpa suspendedJpa = new SubscriptionJpa(
                1L, 50L, 1L, SubscriptionStatus.SUSPENDED,
                LocalDate.now(), LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(1),
                "NGN", null, null, null, ZonedDateTime.now(), ZonedDateTime.now(), 0L
        );
        when(springDataRepo.findByOrganizationId(50L)).thenReturn(Optional.of(suspendedJpa));

        assertFalse(adapter.hasActiveSubscription(50L));
        assertEquals(EntitlementQueryPort.SubscriptionStatus.SUSPENDED, adapter.getSubscriptionStatus(50L));
        assertTrue(adapter.isSubscriptionSuspended(50L));
    }

    @Test
    void methods_returnDefaults_whenNotFoundOrNull() {
        when(springDataRepo.findByOrganizationId(999L)).thenReturn(Optional.empty());

        assertFalse(adapter.hasActiveSubscription(999L));
        assertNull(adapter.getSubscriptionStatus(999L));
        assertFalse(adapter.isSubscriptionSuspended(999L));

        assertFalse(adapter.hasActiveSubscription(null));
        assertNull(adapter.getSubscriptionStatus(null));
        assertFalse(adapter.isSubscriptionSuspended(null));
    }
}
