package com.atlashub.iam.application.commands.IssueApiKey;

import com.atlashub.iam.domain.repositories.ApiKeyRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class IssueApiKeyHandler extends Command<IssueApiKeyCommand, IssuedApiKeyResult> {

    private static final Logger log = LoggerFactory.getLogger(IssueApiKeyHandler.class);
    private final ApiKeyRepository apiKeyRepository;

    public IssueApiKeyHandler(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    public IssuedApiKeyResult execute(IssueApiKeyCommand command) {
        log.info("Executing IssueApiKeyCommand");
        
        // TODO: Implement orchestration logic
        // DO NOT implement business logic here. Delegate to Entities, Domain Services, or Ports.

        return null;
    }
}
