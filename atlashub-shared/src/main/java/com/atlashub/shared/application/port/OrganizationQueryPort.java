package com.atlashub.shared.application.port;

import java.util.Optional;

public interface OrganizationQueryPort {
    Optional<OrganizationDto> findById(Long orgId);
    boolean existsById(Long orgId);
    String getBaseCurrency(Long orgId);
    String getCountry(Long orgId);

    record OrganizationDto(
            Long id,
            String businessName,
            String businessType,
            String businessSize,
            String industry,
            String description,
            String logoUrl,
            String websiteUrl,
            String country,
            String baseCurrency
    ) {
    }
}
