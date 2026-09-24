package com.atlashub.accounts.infrastructure.services;

import com.atlashub.accounts.infrastructure.persistence.entities.OrganizationJPA;
import com.atlashub.accounts.infrastructure.persistence.repositories.SpringDataOrganizationRepository;
import com.atlashub.shared.application.port.OrganizationQueryPort;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class OrganizationQueryPortAdapter implements OrganizationQueryPort {

    public static final String ORGANIZATION_BY_ID_KEY_PREFIX = "cache:organization:id:";

    private final SpringDataOrganizationRepository springDataRepo;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public OrganizationQueryPortAdapter(SpringDataOrganizationRepository springDataRepo,
                                        StringRedisTemplate redisTemplate,
                                        ObjectMapper objectMapper,
                                        @Value("${atlashub.cache.organization-ttl:PT12H}") Duration ttl) {
        this.springDataRepo = springDataRepo;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = ttl;
    }

    @Override
    public Optional<OrganizationDto> findById(Long orgId) {
        String key = ORGANIZATION_BY_ID_KEY_PREFIX + orgId;
        Optional<OrganizationDto> cached = readOrganization(key);
        if (cached.isPresent()) {
            return cached;
        }

        Optional<OrganizationDto> organization = springDataRepo.findById(orgId).map(this::toDto);
        organization.ifPresent(this::cacheOrganization);
        return organization;
    }

    @Override
    public boolean existsById(Long orgId) {
        try {
            if (redisTemplate.hasKey(ORGANIZATION_BY_ID_KEY_PREFIX + orgId)) {
                return true;
            }
        } catch (RuntimeException ignored) {
        }
        return springDataRepo.existsById(orgId);
    }

    @Override
    public String getBaseCurrency(Long orgId) {
        return findById(orgId)
                .map(OrganizationDto::baseCurrency)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
    }

    @Override
    public String getCountry(Long orgId) {
        return findById(orgId)
                .map(OrganizationDto::country)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
    }

    private Optional<OrganizationDto> readOrganization(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return Optional.empty();
            }

            return Optional.of(objectMapper.readValue(json, OrganizationDto.class));
        } catch (JsonProcessingException e) {
            redisTemplate.delete(key);
            return Optional.empty();
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private void cacheOrganization(OrganizationDto organization) {
        try {
            String json = objectMapper.writeValueAsString(organization);
            redisTemplate.opsForValue().set(
                    ORGANIZATION_BY_ID_KEY_PREFIX + organization.id(), json, ttl.toSeconds(), TimeUnit.SECONDS);
        } catch (JsonProcessingException | RuntimeException ignored) {
        }
    }

    private OrganizationDto toDto(OrganizationJPA jpa) {
        return new OrganizationDto(
                jpa.getId(),
                jpa.getBusinessName(),
                jpa.getBusinessType(),
                jpa.getBusinessSize(),
                jpa.getIndustry(),
                jpa.getDescription(),
                jpa.getLogoUrl(),
                jpa.getWebsiteUrl(),
                jpa.getCountry(),
                jpa.getBaseCurrency()
        );
    }
}
