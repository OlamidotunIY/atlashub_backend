package com.atlashub.iam.application.commands.RevokeApiKey;

import com.atlashub.iam.domain.entities.ApiKey;
import com.atlashub.iam.domain.repositories.ApiKeyRepository;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.exception.AuthorizationException;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RevokeApiKeyHandler extends Command<RevokeApiKeyCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(RevokeApiKeyHandler.class);

    private final ApiKeyRepository apiKeyRepository;

    public RevokeApiKeyHandler(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    public Void execute(RevokeApiKeyCommand command) {
        log.info("Executing RevokeApiKeyCommand");
        
        ApiKey apiKey = apiKeyRepository.findById(command.keyId())
                .orElseThrow(() -> new NotFoundException("Api key not found with ID: " + command.keyId()));

        if (!apiKey.getOrganizationId().equals(command.orgId())) {
            throw new AuthorizationException("API key does not belong to the specified organization.");
        }

        apiKey.revoke(command.requestedByUserId());
        apiKeyRepository.save(apiKey);

        return null;
    }
}
