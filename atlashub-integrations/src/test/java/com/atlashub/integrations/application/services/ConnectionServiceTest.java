package com.atlashub.integrations.application.services;

import com.atlashub.integrations.application.port.ConnectionVerifier;
import com.atlashub.integrations.application.port.CredentialVaultPort;
import com.atlashub.integrations.domain.entities.ProviderAuthorization;
import com.atlashub.integrations.domain.valueobject.Capability;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.integrations.infrastructure.configuration.CsvProviderCatalogAdapter;
import com.atlashub.integrations.support.MemoryConnectionRepository;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionServiceTest {
    @Test
    void testAndLiveAuthorizationsBelongToOneProviderConnection() {
        var repository = new MemoryConnectionRepository();
        var service = new ConnectionService(repository, new Vault(), new CsvProviderCatalogAdapter());

        var test = service.connectCredential(10L, Provider.PAYSTACK, "NG", "sk_test_example");
        var live = service.connectCredential(10L, Provider.PAYSTACK, "NG", "sk_live_example");

        assertEquals(test.connection().id(), live.connection().id());
        assertNotEquals(test.authorization().id(), live.authorization().id());
        assertEquals(2, service.list(10L).size());
    }

    @Test
    void environmentIsDerivedFromCredential() {
        var service = new ConnectionService(
                new MemoryConnectionRepository(), new Vault(), new CsvProviderCatalogAdapter());

        assertEquals(ApiEnvironment.LIVE,
                service.connectCredential(10L, Provider.STRIPE, "US", "sk_live_example")
                        .authorization().environment());
        assertThrows(IllegalArgumentException.class,
                () -> service.connectCredential(10L, Provider.PAYSTACK, "NG", "unknown"));
    }

    @Test
    void verificationActivatesOnlyProviderConfirmedCapabilities() {
        var repository = new MemoryConnectionRepository();
        ConnectionVerifier verifier = new ConnectionVerifier() {
            @Override
            public Provider provider() {
                return Provider.PAYSTACK;
            }

            @Override
            public Verification verify(ApiEnvironment environment, String country, String credential,
                                       Set<Capability> documentedCapabilities) {
                assertEquals("secret", credential);
                return new Verification("merchant", Set.of(Capability.CARD_COLLECTION));
            }
        };
        var service = new ConnectionService(repository, new Vault(), new CsvProviderCatalogAdapter(), List.of(verifier));

        var pending = service.connectCredential(10L, Provider.PAYSTACK, "NG", "sk_test_example");
        var active = service.verify(10L, pending.authorization().id());

        assertEquals(ProviderAuthorization.Status.ACTIVE, active.authorization().status());
        assertEquals(Set.of(Capability.CARD_COLLECTION), active.authorization().activeCapabilities());
    }

    @Test
    void disconnectRevokesEveryEnvironment() {
        var service = new ConnectionService(
                new MemoryConnectionRepository(), new Vault(), new CsvProviderCatalogAdapter());
        var value = service.connectCredential(10L, Provider.PAYSTACK, "NG", "sk_test_example");
        service.connectCredential(10L, Provider.PAYSTACK, "NG", "sk_live_example");

        service.disconnect(10L, value.connection().id());

        assertTrue(service.list(10L).stream()
                .allMatch(item -> item.authorization().status() == ProviderAuthorization.Status.REVOKED));
    }

    static final class Vault implements CredentialVaultPort {
        @Override
        public String store(String credential) {
            return "vault-" + credential;
        }

        @Override
        public String read(String reference) {
            return "secret";
        }
    }
}
