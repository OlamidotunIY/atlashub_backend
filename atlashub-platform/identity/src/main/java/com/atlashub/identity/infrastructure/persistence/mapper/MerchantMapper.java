package com.atlashub.identity.infrastructure.persistence.mapper;

import com.atlashub.identity.domain.model.ComplianceStatus;
import com.atlashub.identity.domain.model.Merchant;
import com.atlashub.identity.domain.model.ComplianceStep;
import com.atlashub.identity.infrastructure.persistence.entity.MerchantJpaEntity;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.springframework.stereotype.Component;

@Component
public class MerchantMapper {

    public MerchantJpaEntity toEntity(Merchant domain) {
        if (domain == null) return null;

        return new MerchantJpaEntity(
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

    public Merchant toDomain(MerchantJpaEntity entity) {
        if (entity == null) return null;

        return new Merchant(
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

