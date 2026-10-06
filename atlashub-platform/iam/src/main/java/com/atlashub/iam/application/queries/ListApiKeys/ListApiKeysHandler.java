package com.atlashub.iam.application.queries.ListApiKeys;

import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlashub.iam.domain.repositories.ApiKeyRepository;
import com.atlashub.iam.domain.entities.ApiKey;
import com.atlashub.shared.application.security.ApiEnvironment;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

@Component
public class ListApiKeysHandler extends Query<ListApiKeysQuery, List<ApiKeyResult>> {

    private static final Logger log = LoggerFactory.getLogger(ListApiKeysHandler.class);
    
    private final ApiKeyRepository apiKeyRepository;

    public ListApiKeysHandler(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('iam:apikeys:manage')")
    public List<ApiKeyResult> execute(ListApiKeysQuery query) {
        log.info("Executing ListApiKeysQuery for orgId: {}, environment: {}", query.orgId(), query.environment());
        
        ApiEnvironment env = query.environment();

        List<ApiKey> keys = env == null
                ? apiKeyRepository.findByOrganizationId(query.orgId())
                : apiKeyRepository.findByOrganizationIdAndEnvironment(query.orgId(), env);
        
        List<ApiKeyResult> results = keys.stream()
            .map(key -> new ApiKeyResult(
                key.getId(),
                key.getOrganizationId(),
                key.getPublicKey(),
                key.getName(),
                key.getEnvironment(),
                key.getRevoked(),
                key.getBoundRoleId(),
                key.getLastUsedAt(),
                key.getCreatedAt()
            ))
            .collect(Collectors.toList());

        return results;
    }
}
