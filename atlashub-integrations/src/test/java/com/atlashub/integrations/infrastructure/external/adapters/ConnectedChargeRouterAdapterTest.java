package com.atlashub.integrations.infrastructure.external.adapters;

import com.atlashub.integrations.application.port.ConnectedChargeAdapter;
import com.atlashub.integrations.application.port.CredentialVaultPort;
import com.atlashub.integrations.domain.entities.ProviderAuthorization;
import com.atlashub.integrations.domain.entities.ProviderConnection;
import com.atlashub.integrations.domain.valueobject.Capability;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.integrations.support.MemoryConnectionRepository;
import com.atlashub.shared.application.port.ChargeProviderPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConnectedChargeRouterAdapterTest {
    @Test
    void resolvesAuthorizationSecretWithoutExposingIt() {
        var repository = repository(ProviderAuthorization.Health.HEALTHY);
        ConnectedChargeAdapter adapter = new ConnectedChargeAdapter() {
            @Override
            public Provider provider() {
                return Provider.PAYSTACK;
            }

            @Override
            public ChargeProviderPort.InitializationResult initialize(
                    ChargeProviderPort.InitializationRequest request, String secret) {
                assertEquals("secret", secret);
                return new ChargeProviderPort.InitializationResult("ref", "url", "access");
            }

            @Override
            public ChargeProviderPort.RefundResult refund(ChargeProviderPort.RefundRequest request, String secret) {
                throw new UnsupportedOperationException();
            }

            @Override
            public ChargeProviderPort.ProviderStatus fetchStatus(String reference, String secret) {
                throw new UnsupportedOperationException();
            }
        };
        var router = new ConnectedChargeRouterAdapter(repository, vault(), List.of(adapter));

        var result = router.initialize(new ChargeProviderPort.InitializationRequest(
                ApiEnvironment.TEST, "a@b.com", BigDecimal.TEN, CurrencyCode.NGN,
                ChargeProviderPort.ChargeChannel.CARD, "order", null, Map.of(), 7L));

        assertEquals("ref", result.providerReference());
    }

    @Test
    void rejectsUnhealthyAuthorization() {
        var router = new ConnectedChargeRouterAdapter(
                repository(ProviderAuthorization.Health.DOWN), vault(), List.of());

        assertThrows(IllegalStateException.class, () -> router.initialize(
                new ChargeProviderPort.InitializationRequest(
                        ApiEnvironment.TEST, "a@b.com", BigDecimal.TEN, CurrencyCode.NGN,
                        ChargeProviderPort.ChargeChannel.CARD, "order", null, Map.of(), 7L)));
    }

    private MemoryConnectionRepository repository(ProviderAuthorization.Health health) {
        var repository = new MemoryConnectionRepository();
        repository.saveConnection(new ProviderConnection(
                3L, 2L, Provider.PAYSTACK, "NG", ProviderConnection.Status.CONNECTED));
        repository.saveAuthorization(new ProviderAuthorization(
                7L, 3L, ApiEnvironment.TEST, "merchant", ProviderAuthorization.Status.ACTIVE, health,
                Set.of(Capability.CARD_COLLECTION), "vault"));
        return repository;
    }

    private CredentialVaultPort vault() {
        return new CredentialVaultPort() {
            @Override
            public String store(String credential) {
                return credential;
            }

            @Override
            public String read(String reference) {
                return "secret";
            }
        };
    }
}
