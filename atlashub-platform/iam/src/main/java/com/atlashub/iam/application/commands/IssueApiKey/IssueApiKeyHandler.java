package com.atlashub.iam.application.commands.IssueApiKey;

import com.atlashub.iam.application.port.ApiSecretProtector;
import com.atlashub.shared.application.port.ComplianceQueryPort;
import com.atlashub.iam.domain.entities.ApiKey;
import com.atlashub.iam.domain.repositories.ApiKeyRepository;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import com.atlashub.iam.domain.exception.CustomRoleNotFoundException;
import com.atlashub.iam.domain.exception.LiveApiKeyUnavailableException;

@Component
public class IssueApiKeyHandler extends Command<IssueApiKeyCommand, IssuedApiKeyResult> {

    private static final Logger log = LoggerFactory.getLogger(IssueApiKeyHandler.class);
    private final ApiKeyRepository apiKeyRepository;
    private final CustomRoleRepository roleRepository;
    private final ComplianceQueryPort complianceQueryPort;
    private final ApiSecretProtector secretProtector;
    private final SecureRandom secureRandom = new SecureRandom();

    public IssueApiKeyHandler(ApiKeyRepository apiKeyRepository,
                              CustomRoleRepository roleRepository,
                              ComplianceQueryPort complianceQueryPort,
                              ApiSecretProtector secretProtector) {
        this.apiKeyRepository = apiKeyRepository;
        this.roleRepository = roleRepository;
        this.complianceQueryPort = complianceQueryPort;
        this.secretProtector = secretProtector;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('iam:apikeys:manage')")
    public IssuedApiKeyResult execute(IssueApiKeyCommand command) {
        log.info("Executing IssueApiKeyCommand");

        ApiEnvironment environment = ApiEnvironment.parse(command.environment());
        if (environment == ApiEnvironment.LIVE && !complianceQueryPort.isApproved(command.orgId())) {
            throw new LiveApiKeyUnavailableException("Live API keys require approved compliance");
        }
        roleRepository.findById(command.boundRoleId())
                .filter(role -> role.getOrganizationId().equals(command.orgId()))
                .orElseThrow(CustomRoleNotFoundException::new);

        String publicKey = "atlas_pk_" + environment.name().toLowerCase() + "_" + randomToken(18);
        String secretKey = "atlas_sk_" + environment.name().toLowerCase() + "_" + randomToken(32);
        ApiKey apiKey = ApiKey.create(
                apiKeyRepository.nextIdentity(), command.orgId(), publicKey, secretProtector.encrypt(secretKey),
                command.name(), environment, command.boundRoleId());
        apiKeyRepository.save(apiKey);
        return new IssuedApiKeyResult(publicKey, secretKey, environment.name());
    }

    private String randomToken(int bytes) {
        byte[] value = new byte[bytes];
        secureRandom.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
