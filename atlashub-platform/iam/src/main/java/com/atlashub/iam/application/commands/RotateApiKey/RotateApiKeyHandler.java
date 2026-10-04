package com.atlashub.iam.application.commands.RotateApiKey;

import com.atlashub.iam.application.commands.IssueApiKey.IssuedApiKeyResult;
import com.atlashub.iam.application.port.ApiSecretProtector;
import com.atlashub.iam.domain.entities.ApiKey;
import com.atlashub.iam.domain.exception.ApiKeyNotFoundException;
import com.atlashub.iam.domain.repositories.ApiKeyRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class RotateApiKeyHandler extends Command<RotateApiKeyCommand, IssuedApiKeyResult> {
    private final ApiKeyRepository repository;
    private final ApiSecretProtector secretProtector;
    private final SecureRandom secureRandom = new SecureRandom();

    public RotateApiKeyHandler(ApiKeyRepository repository, ApiSecretProtector secretProtector) {
        this.repository = repository;
        this.secretProtector = secretProtector;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('iam:apikeys:manage')")
    public IssuedApiKeyResult execute(RotateApiKeyCommand command) {
        ApiKey current = repository.findByIdAndOrganizationIdForUpdate(command.keyId(), command.organizationId())
                .orElseThrow(ApiKeyNotFoundException::new);
        current.revoke(command.requestedByUserId());
        repository.save(current);

        String environment = current.getEnvironment().name().toLowerCase();
        String publicKey = "atlas_pk_" + environment + "_" + randomToken(18);
        String secretKey = "atlas_sk_" + environment + "_" + randomToken(32);
        String name = command.name() == null || command.name().isBlank() ? current.getName() : command.name();
        ApiKey replacement = ApiKey.create(repository.nextIdentity(), current.getOrganizationId(), publicKey,
                secretProtector.encrypt(secretKey), name, current.getEnvironment(), current.getBoundRoleId());
        repository.save(replacement);
        return new IssuedApiKeyResult(publicKey, secretKey, current.getEnvironment().name());
    }

    private String randomToken(int bytes) {
        byte[] value = new byte[bytes];
        secureRandom.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
