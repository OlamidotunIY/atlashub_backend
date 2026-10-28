package com.atlashub.integrations.infrastructure.external.adapters;

import com.atlashub.integrations.application.port.ConnectedChargeAdapter;
import com.atlashub.integrations.application.port.CredentialVaultPort;
import com.atlashub.integrations.domain.entities.ProviderAuthorization;
import com.atlashub.integrations.domain.entities.ProviderConnection;
import com.atlashub.integrations.domain.repositories.ConnectionRepository;
import com.atlashub.integrations.domain.valueobject.Capability;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.shared.application.port.ChargeProviderPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ConnectedChargeRouterAdapter implements ChargeProviderPort {
    private final ConnectionRepository repository;
    private final CredentialVaultPort vault;
    private final Map<Provider, ConnectedChargeAdapter> adapters = new EnumMap<>(Provider.class);

    public ConnectedChargeRouterAdapter(ConnectionRepository repository, CredentialVaultPort vault,
                                        List<ConnectedChargeAdapter> adapters) {
        this.repository = repository;
        this.vault = vault;
        for (ConnectedChargeAdapter adapter : adapters)
            if (this.adapters.put(adapter.provider(), adapter) != null)
                throw new IllegalStateException("Multiple charge adapters registered for " + adapter.provider());
    }

    public InitializationResult initialize(InitializationRequest request) {
        ProviderAuthorization a = require(request.providerConnectionId(), request.environment(),
                request.channel() == ChargeChannel.CARD ? Capability.CARD_COLLECTION : Capability.USSD_COLLECTION);
        return adapter(a).initialize(request, vault.read(a.credentialReference()));
    }

    public RefundResult refund(RefundRequest request) {
        ProviderAuthorization a = require(request.providerConnectionId(), request.environment(), Capability.REFUND);
        return adapter(a).refund(request, vault.read(a.credentialReference()));
    }

    public ProviderStatus fetchStatus(Long id, String reference, ApiEnvironment environment) {
        ProviderAuthorization a = requireActive(id, environment);
        return adapter(a).fetchStatus(reference, vault.read(a.credentialReference()));
    }

    private ProviderAuthorization require(Long id, ApiEnvironment env, Capability capability) {
        ProviderAuthorization a = requireActive(id, env);
        if (!a.eligible(capability))
            throw new IllegalStateException("Provider authorization does not support " + capability);
        return a;
    }

    private ProviderAuthorization requireActive(Long id, ApiEnvironment env) {
        if (id == null) throw new IllegalArgumentException("Provider authorization is required");
        ProviderAuthorization a = repository.findAuthorization(id)
                .orElseThrow(() -> new IllegalArgumentException("Provider authorization not found"));
        if (a.environment() != env || a.status() != ProviderAuthorization.Status.ACTIVE ||
                a.health() != ProviderAuthorization.Health.HEALTHY)
            throw new IllegalStateException("Provider authorization is unavailable");
        return a;
    }

    private ConnectedChargeAdapter adapter(ProviderAuthorization a) {
        ProviderConnection c = repository.findConnection(a.connectionId()).orElseThrow();
        ConnectedChargeAdapter adapter = adapters.get(c.provider());
        if (adapter == null) throw new IllegalStateException("No charge adapter is installed for " + c.provider());
        return adapter;
    }
}
