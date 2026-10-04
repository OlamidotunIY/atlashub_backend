package com.atlashub.iam.application.commands.RevokeApiKey;

import com.atlashub.iam.domain.entities.ApiKey;
import com.atlashub.iam.domain.repositories.ApiKeyRepository;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.iam.domain.exception.ApiKeyNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RevokeApiKeyHandler extends Command<RevokeApiKeyCommand, ApiKey> {

    private static final Logger log = LoggerFactory.getLogger(RevokeApiKeyHandler.class);

    private final ApiKeyRepository apiKeyRepository;

    public RevokeApiKeyHandler(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('iam:apikeys:manage')")
    public ApiKey execute(RevokeApiKeyCommand command) {
        log.info("Executing RevokeApiKeyCommand");
        
        ApiKey apiKey = apiKeyRepository.findByIdAndOrganizationIdForUpdate(command.keyId(), command.orgId())
                .orElseThrow(ApiKeyNotFoundException::new);

        apiKey.revoke(command.requestedByUserId());
        apiKeyRepository.save(apiKey);

        return apiKey;
    }
}
