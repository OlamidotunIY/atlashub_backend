package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.command.RevokeApiKeyCommand;

import com.atlashub.shared.usecase.BaseUseCase;

import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.ApiKey;
import com.atlashub.identity.domain.repository.ApiKeyRepository;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.NotFoundException;

@Service
public class RevokeApiKeyUseCase extends BaseUseCase<RevokeApiKeyCommand, Void> {
    private static final Logger log = LoggerFactory.getLogger(RevokeApiKeyUseCase.class);


    private final ApiKeyRepository apiKeyRepository;
    private final DomainEventPublisher eventPublisher;

    public RevokeApiKeyUseCase(ApiKeyRepository apiKeyRepository, DomainEventPublisher eventPublisher) {
        this.apiKeyRepository = apiKeyRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Void execute(RevokeApiKeyCommand command) {
        log.info("Executing RevokeApiKeyUseCase");

        ApiKey apiKey = apiKeyRepository.findById(command.keyId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.API_KEY_NOT_FOUND, "API Key not found"));

        if (!apiKey.getMerchantId().equals(command.authenticatedMerchantId())) {
            throw new NotFoundException(IdentityErrorCode.API_KEY_NOT_FOUND, "API Key not found");
        }

        apiKey.revoke();

        apiKeyRepository.save(apiKey);
        publishEvents(apiKey, eventPublisher);
    
        return null;
    }
}



