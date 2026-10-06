package com.atlashub.iam.infrastructure.persistence.adapters;

import com.atlashub.iam.application.security.ApiKeyAuthenticationService;
import com.atlashub.shared.application.port.DomainEventPublisher;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiKeyQueryPortAdapterTest {

    @Test
    void delegates_authentication_and_maps_the_shared_contract() {
        ApiKeyAuthenticationService service = mock(ApiKeyAuthenticationService.class);
        DomainEventPublisher publisher = mock(DomainEventPublisher.class);
        when(service.authenticate("atlas_pk_test_key", "canonical", "signature"))
                .thenReturn(Optional.of(new ApiKeyAuthenticationService.AuthenticatedApiKey(
                        42L, "TEST", "atlas_pk_test_key", Set.of("pay:charges:create"))));
        ApiKeyQueryPortAdapter adapter = new ApiKeyQueryPortAdapter(service, publisher);

        var authenticated = adapter.authenticate("atlas_pk_test_key", "canonical", "signature");

        assertTrue(authenticated.isPresent());
        assertEquals(42L, authenticated.orElseThrow().organizationId());
        assertEquals("TEST", authenticated.orElseThrow().environment());
        assertEquals(Set.of("pay:charges:create"), authenticated.orElseThrow().permissions());
        verify(service).authenticate("atlas_pk_test_key", "canonical", "signature");
        verify(publisher).publish(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void preserves_an_authentication_failure() {
        ApiKeyAuthenticationService service = mock(ApiKeyAuthenticationService.class);
        DomainEventPublisher publisher = mock(DomainEventPublisher.class);
        when(service.authenticate("unknown", "canonical", "signature")).thenReturn(Optional.empty());
        ApiKeyQueryPortAdapter adapter = new ApiKeyQueryPortAdapter(service, publisher);

        assertTrue(adapter.authenticate("unknown", "canonical", "signature").isEmpty());
        verify(publisher, org.mockito.Mockito.never()).publish(org.mockito.ArgumentMatchers.any());
    }
}
