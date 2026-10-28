package com.atlashub.integrations.application;

import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConnectionServiceTest {
    @Test
    void test_and_live_authorizations_belong_to_one_provider_connection() {
        MemoryRepository repo = new MemoryRepository();
        ConnectionService service = new ConnectionService(repo, new Vault(), new ProviderCatalog());
        var test = service.connectCredential(10L, Provider.PAYSTACK, "NG", "sk_test_example");
        var live = service.connectCredential(10L, Provider.PAYSTACK, "NG", "sk_live_example");
        assertEquals(test.connection().id(), live.connection().id());
        assertNotEquals(test.authorization().id(), live.authorization().id());
        assertEquals(2, service.list(10L).size());
    }

    @Test
    void environment_is_derived_from_the_credential() {
        ConnectionService service = new ConnectionService(new MemoryRepository(), new Vault(), new ProviderCatalog());
        assertEquals(ApiEnvironment.LIVE,
                service.connectCredential(10L, Provider.STRIPE, "US", "sk_live_example").authorization().environment());
        assertThrows(IllegalArgumentException.class,
                () -> service.connectCredential(10L, Provider.PAYSTACK, "NG", "unknown"));
    }

    @Test
    void verification_activates_only_provider_confirmed_capabilities() {
        MemoryRepository repo = new MemoryRepository();
        ConnectionVerifier verifier = new ConnectionVerifier() {
            public Provider provider() {
                return Provider.PAYSTACK;
            }

            public Verification verify(ApiEnvironment e, String c, String credential, Set<Capability> documented) {
                assertEquals("secret", credential);
                return new Verification("merchant", Set.of(Capability.CARD_COLLECTION));
            }
        };
        ConnectionService service = new ConnectionService(repo, new Vault(), new ProviderCatalog(), List.of(verifier));
        var pending = service.connectCredential(10L, Provider.PAYSTACK, "NG", "sk_test_example");
        var active = service.verify(10L, pending.authorization().id());
        assertEquals(ProviderAuthorization.Status.ACTIVE, active.authorization().status());
        assertEquals(Set.of(Capability.CARD_COLLECTION), active.authorization().activeCapabilities());
    }

    @Test
    void disconnect_revokes_every_environment() {
        MemoryRepository repo = new MemoryRepository();
        ConnectionService service = new ConnectionService(repo, new Vault(), new ProviderCatalog());
        var value = service.connectCredential(10L, Provider.PAYSTACK, "NG", "sk_test_example");
        service.connectCredential(10L, Provider.PAYSTACK, "NG", "sk_live_example");
        service.disconnect(10L, value.connection().id());
        assertTrue(service.list(10L).stream()
                .allMatch(v -> v.authorization().status() == ProviderAuthorization.Status.REVOKED));
    }

    static class Vault implements CredentialVaultPort {
        public String store(String v) {
            return "vault-" + v;
        }

        public String read(String r) {
            return "secret";
        }
    }

    static class MemoryRepository implements ConnectionRepository {
        List<ProviderConnection> cs = new ArrayList<>();
        List<ProviderAuthorization> as = new ArrayList<>();

        public ProviderConnection saveConnection(ProviderConnection c) {
            ProviderConnection v =
                    new ProviderConnection(c.id() == null ? (long) cs.size() + 1 : c.id(), c.organizationId(),
                            c.provider(), c.merchantCountry(), c.status());
            cs.removeIf(x -> x.id().equals(v.id()));
            cs.add(v);
            return v;
        }

        public Optional<ProviderConnection> findConnection(Long id) {
            return cs.stream().filter(v -> v.id().equals(id)).findFirst();
        }

        public Optional<ProviderConnection> findConnection(Long org, Provider p, String country) {
            return cs.stream().filter(v -> v.organizationId().equals(org) && v.provider() == p &&
                    v.merchantCountry().equals(country)).findFirst();
        }

        public List<ProviderConnection> findConnections(Long org) {
            return cs.stream().filter(v -> v.organizationId().equals(org)).toList();
        }

        public ProviderAuthorization saveAuthorization(ProviderAuthorization a) {
            ProviderAuthorization v =
                    new ProviderAuthorization(a.id() == null ? (long) as.size() + 1 : a.id(), a.connectionId(),
                            a.environment(), a.externalMerchantId(), a.status(), a.health(), a.activeCapabilities(),
                            a.credentialReference());
            as.removeIf(x -> x.id().equals(v.id()));
            as.add(v);
            return v;
        }

        public Optional<ProviderAuthorization> findAuthorization(Long id) {
            return as.stream().filter(v -> v.id().equals(id)).findFirst();
        }

        public Optional<ProviderAuthorization> findAuthorization(Long id, ApiEnvironment e) {
            return as.stream().filter(v -> v.connectionId().equals(id) && v.environment() == e).findFirst();
        }

        public Optional<ProviderAuthorization> findAuthorization(ApiEnvironment e, String merchant) {
            return as.stream().filter(v -> v.environment() == e && Objects.equals(v.externalMerchantId(), merchant))
                    .findFirst();
        }

        public List<ProviderAuthorization> findAuthorizations(Long id) {
            return as.stream().filter(v -> v.connectionId().equals(id)).toList();
        }
    }
}
