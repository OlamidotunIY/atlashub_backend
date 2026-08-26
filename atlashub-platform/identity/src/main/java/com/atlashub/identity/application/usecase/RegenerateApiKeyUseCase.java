package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.command.RegenerateApiKeyCommand;

import com.atlashub.shared.usecase.BaseUseCase;

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
public class RegenerateApiKeyUseCase extends BaseUseCase<RegenerateApiKeyCommand, String> {
    private static final Logger log = LoggerFactory.getLogger(RegenerateApiKeyUseCase.class);


    private final OrganizationRepository OrganizationRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final DomainEventPublisher eventPublisher;

    public RegenerateApiKeyUseCase(
            OrganizationRepository OrganizationRepository,
            ApiKeyRepository apiKeyRepository,
            DomainEventPublisher eventPublisher) {
        this.OrganizationRepository = OrganizationRepository;
        this.apiKeyRepository = apiKeyRepository;
                this.eventPublisher = eventPublisher;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String execute(RegenerateApiKeyCommand command) {
        log.info("Executing RegenerateApiKeyUseCase");

        if (command.environment() == ApiEnvironment.LIVE) {
            Organization Organization = OrganizationRepository.findById(command.authenticatedOrganizationId())
                    .orElseThrow(() -> new NotFoundException(IdentityErrorCode.Organization_NOT_FOUND, "Organization not found"));
            
            if (Organization.getComplianceStatus() != com.atlashub.identity.domain.model.ComplianceStatus.APPROVED) {
                throw new BusinessRuleException(IdentityErrorCode.LIVE_KEYS_REQUIRE_COMPLIANCE_APPROVED, "Live keys require compliance to be approved");
            }
        }

        apiKeyRepository.findByOrganizationIdAndKeyTypeAndEnvironmentAndActiveTrue(
                command.authenticatedOrganizationId(), command.keyType(), command.environment()
        ).ifPresent(existingKey -> {
            existingKey.revoke();
            apiKeyRepository.save(existingKey);
            publishEvents(existingKey, eventPublisher);
        });

        String envPrefix = command.environment() == ApiEnvironment.LIVE ? "live_" : "test_";
        String typePrefix = command.keyType() == KeyType.PUBLIC ? "pk_" : "sk_";
        String prefix = typePrefix + envPrefix;
        
        String rawKey = prefix + UUID.randomUUID().toString().replace("-", "");

        String keyHash;
        String displayValue;

        if (command.keyType() == KeyType.PUBLIC) {
            keyHash = rawKey;
            displayValue = rawKey;
        } else {
            keyHash = com.atlashub.shared.util.HashingUtils.sha256Hex(rawKey);
            displayValue = prefix + "****" + rawKey.substring(rawKey.length() - 4);
        }

        ApiKey newKey = new ApiKey(apiKeyRepository.nextIdentity(),
                command.authenticatedOrganizationId(),
                command.keyType(),
                command.environment(),
                keyHash,
                displayValue,
                prefix
        );

        apiKeyRepository.save(newKey);
        publishEvents(newKey, eventPublisher);

        return rawKey;
    }
}




