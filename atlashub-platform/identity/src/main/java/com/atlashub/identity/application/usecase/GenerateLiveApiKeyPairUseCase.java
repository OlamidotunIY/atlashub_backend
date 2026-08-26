package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.command.GenerateLiveApiKeyPairCommand;

import com.atlashub.shared.usecase.BaseUseCase;

import com.atlashub.identity.application.dto.ApiKeyPairResult;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.ApiEnvironment;
import com.atlashub.identity.domain.model.ApiKey;
import com.atlashub.identity.domain.model.KeyType;
import com.atlashub.identity.domain.model.Organization;
import com.atlashub.identity.domain.repository.ApiKeyRepository;
import com.atlashub.identity.domain.repository.OrganizationRepository;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.BusinessRuleException;
import com.atlashub.shared.exception.NotFoundException;

import java.util.UUID;

@Service
public class GenerateLiveApiKeyPairUseCase extends BaseUseCase<GenerateLiveApiKeyPairCommand, ApiKeyPairResult> {
    private static final Logger log = LoggerFactory.getLogger(GenerateLiveApiKeyPairUseCase.class);


    private final OrganizationRepository OrganizationRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final DomainEventPublisher eventPublisher;

    public GenerateLiveApiKeyPairUseCase(
            OrganizationRepository OrganizationRepository,
            ApiKeyRepository apiKeyRepository,
            DomainEventPublisher eventPublisher) {
        this.OrganizationRepository = OrganizationRepository;
        this.apiKeyRepository = apiKeyRepository;
                this.eventPublisher = eventPublisher;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ApiKeyPairResult execute(GenerateLiveApiKeyPairCommand command) {
        log.info("Executing GenerateLiveApiKeyPairUseCase");

        Organization Organization = OrganizationRepository.findById(command.OrganizationId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.Organization_NOT_FOUND, "Organization not found"));

        if (Organization.getComplianceStatus() != com.atlashub.identity.domain.model.ComplianceStatus.APPROVED) {
            throw new BusinessRuleException(IdentityErrorCode.LIVE_KEYS_REQUIRE_COMPLIANCE_APPROVED, "Live keys require compliance to be approved");
        }

        String rawPublicKey = "pk_live_" + UUID.randomUUID().toString().replace("-", "");
        String rawSecretKey = "sk_live_" + UUID.randomUUID().toString().replace("-", "");

        ApiKey publicKey = new ApiKey(apiKeyRepository.nextIdentity(),
                command.OrganizationId(),
                KeyType.PUBLIC,
                ApiEnvironment.LIVE,
                rawPublicKey,
                rawPublicKey,
                "pk_live_"
        );

        String secretHash = com.atlashub.shared.util.HashingUtils.sha256Hex(rawSecretKey);
        String secretDisplay = "sk_live_****" + rawSecretKey.substring(rawSecretKey.length() - 4);

        ApiKey secretKey = new ApiKey(apiKeyRepository.nextIdentity(),
                command.OrganizationId(),
                KeyType.SECRET,
                ApiEnvironment.LIVE,
                secretHash,
                secretDisplay,
                "sk_live_"
        );

        apiKeyRepository.save(publicKey);
        apiKeyRepository.save(secretKey);

        publishEvents(publicKey, eventPublisher);
        publishEvents(secretKey, eventPublisher);

        return new ApiKeyPairResult(rawPublicKey, rawSecretKey);
    }
}




