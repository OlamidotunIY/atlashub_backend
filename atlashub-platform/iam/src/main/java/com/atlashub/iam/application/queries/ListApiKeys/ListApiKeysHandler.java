package com.atlashub.iam.application.queries.ListApiKeys;

import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlashub.iam.domain.repositories.ApiKeyRepository;
import com.atlashub.iam.domain.entities.ApiKey;
import com.atlashub.iam.domain.valueobject.ApiEnvironment;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListApiKeysHandler extends Query<ListApiKeysQuery, List<ApiKeyResult>> {

    private static final Logger log = LoggerFactory.getLogger(ListApiKeysHandler.class);
    
    private final ApiKeyRepository apiKeyRepository;

    public ListApiKeysHandler(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    public List<ApiKeyResult> execute(ListApiKeysQuery query) {
        log.info("Executing ListApiKeysQuery for orgId: {}, environment: {}", query.orgId(), query.environment());
        
        ApiEnvironment env = null;
        if (query.environment() != null) {
            env = ApiEnvironment.valueOf(query.environment().toUpperCase());
        }

        List<ApiKey> keys = apiKeyRepository.findByOrganizationIdAndEnvironment(query.orgId(), env);
        
        List<ApiKeyResult> results = keys.stream()
            .map(key -> new ApiKeyResult(
                key.getId(),
                key.getOrganizationId(),
                key.getPublicKey(),
                key.getName(),
                key.getEnvironment().name(),
                key.getIsRevoked(),
                key.getLastUsedAt(),
                key.getCreatedAt()
            ))
            .collect(Collectors.toList());

        return results;
    }
}
