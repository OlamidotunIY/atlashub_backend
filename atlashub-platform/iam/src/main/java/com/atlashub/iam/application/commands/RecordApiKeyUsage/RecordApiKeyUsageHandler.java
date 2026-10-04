package com.atlashub.iam.application.commands.RecordApiKeyUsage;

import com.atlashub.iam.domain.exception.ApiKeyNotFoundException;
import com.atlashub.iam.domain.repositories.ApiKeyRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RecordApiKeyUsageHandler extends Command<RecordApiKeyUsageCommand, Void> {
    private final ApiKeyRepository repository;

    public RecordApiKeyUsageHandler(ApiKeyRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(RecordApiKeyUsageCommand command) {
        var key = repository.findByPublicKey(command.publicKey())
                .filter(found -> found.getOrganizationId().equals(command.organizationId()))
                .orElseThrow(ApiKeyNotFoundException::new);
        key.recordUsage();
        repository.save(key);
        return null;
    }
}
