package com.atlashub.integrations.infrastructure.persistence.adapters;

import com.atlashub.integrations.domain.repositories.ConnectionRepository;
import com.atlashub.integrations.domain.entities.ProviderAuthorization;
import com.atlashub.integrations.domain.entities.ProviderConnection;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.integrations.infrastructure.persistence.entities.AuthorizationJpa;
import com.atlashub.integrations.infrastructure.persistence.entities.ConnectionJpa;
import com.atlashub.integrations.infrastructure.persistence.mappers.ProviderConnectionMapper;
import com.atlashub.integrations.infrastructure.persistence.repositories.SpringDataAuthorizationRepository;
import com.atlashub.integrations.infrastructure.persistence.repositories.SpringDataConnectionRepository;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class ConnectionRepositoryAdapter implements ConnectionRepository {
    private final SpringDataConnectionRepository connections;
    private final SpringDataAuthorizationRepository authorizations;
    private final ProviderConnectionMapper mapper;
    public ConnectionRepositoryAdapter(SpringDataConnectionRepository connections, SpringDataAuthorizationRepository authorizations,
                                       ProviderConnectionMapper mapper) {
        this.connections=connections; this.authorizations=authorizations; this.mapper=mapper;
    }
    public ProviderConnection saveConnection(ProviderConnection value) {
        ConnectionJpa row=value.id()==null?new ConnectionJpa():connections.findById(value.id()).orElseThrow();
        mapper.copy(value, row);
        return mapper.toDomain(connections.save(row));
    }
    public Optional<ProviderConnection> findConnection(Long id){return connections.findById(id).map(mapper::toDomain);}
    public Optional<ProviderConnection> findConnection(Long org,Provider provider,String country){return connections
            .findByOrganizationIdAndProviderAndMerchantCountry(org,provider,country).map(mapper::toDomain);}
    public List<ProviderConnection> findConnections(Long org){return connections.findByOrganizationId(org).stream().map(mapper::toDomain).toList();}
    public ProviderAuthorization saveAuthorization(ProviderAuthorization value){
        AuthorizationJpa row=value.id()==null?new AuthorizationJpa():authorizations.findById(value.id()).orElseThrow();
        mapper.copy(value, row);
        return mapper.toDomain(authorizations.save(row));
    }
    public Optional<ProviderAuthorization> findAuthorization(Long id){return authorizations.findById(id).map(mapper::toDomain);}
    public Optional<ProviderAuthorization> findAuthorization(Long id,ApiEnvironment env){return authorizations.findByConnectionIdAndEnvironment(id,env).map(mapper::toDomain);}
    public Optional<ProviderAuthorization> findAuthorization(ApiEnvironment env,String merchant){return authorizations.findByEnvironmentAndExternalMerchantId(env,merchant).map(mapper::toDomain);}
    public List<ProviderAuthorization> findAuthorizations(Long id){return authorizations.findByConnectionId(id).stream().map(mapper::toDomain).toList();}
}
