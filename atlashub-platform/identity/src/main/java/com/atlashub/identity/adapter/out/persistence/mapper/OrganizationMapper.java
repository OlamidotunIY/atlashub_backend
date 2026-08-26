package com.atlashub.identity.adapter.out.persistence.mapper;

import com.atlashub.identity.domain.model.ComplianceStatus;
import com.atlashub.identity.domain.model.Organization;
import com.atlashub.identity.domain.model.ComplianceStep;
import com.atlashub.identity.adapter.out.persistence.entity.OrganizationJpaEntity;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {

    public OrganizationJpaEntity toEntity(Organization domain) {
        if (domain == null) return null;

        return new OrganizationJpaEntity(
                domain.getId(),
                domain.getCountry().name(),
                domain.getBusinessName(),
                domain.getFirstName(),
                domain.getLastName(),
                domain.getEmail().value(),
                domain.getPhone() != null ? domain.getPhone().value() : null,
                domain.getBusinessType(),
                domain.getComplianceStatus(),
                domain.getComplianceStep() != null ? domain.getComplianceStep().name() : null,
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public Organization toDomain(OrganizationJpaEntity entity) {
        if (entity == null) return null;

        return new Organization(
                entity.getId(),
                mapCountry(entity.getCountry()),
                entity.getBusinessName(),
                entity.getFirstName(),
                entity.getLastName(),
                new EmailAddress(entity.getEmail()),
                entity.getPhone() != null ? new PhoneNumber(entity.getPhone()) : null,
                entity.getBusinessType(),
                entity.getComplianceStatus() != null ? ComplianceStatus.valueOf(entity.getComplianceStatus().name()) : ComplianceStatus.NOT_STARTED,
                entity.getComplianceStep() != null ? ComplianceStep.valueOf(entity.getComplianceStep()) : null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private Country mapCountry(String countryStr) {
        Country country = Country.fromString(countryStr);
        return country != null ? country : Country.NIGERIA;
    }
}

