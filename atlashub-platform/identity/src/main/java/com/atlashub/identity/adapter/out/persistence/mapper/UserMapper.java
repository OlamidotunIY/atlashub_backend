package com.atlashub.identity.adapter.out.persistence.mapper;

import com.atlashub.identity.adapter.out.persistence.entity.UserJpaEntity;
import com.atlashub.identity.domain.model.User;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserJpaEntity toEntity(User domain) {
        if (domain == null) return null;

        return new UserJpaEntity(
                domain.getId(),
                domain.getFirstName(),
                domain.getLastName(),
                domain.getEmail().value(),
                domain.getPhone() != null ? domain.getPhone().value() : null,
                domain.getCountry().name(),
                domain.getActiveOrganizationId(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;

        return new User(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                new EmailAddress(entity.getEmail()),
                entity.getPhone() != null ? new PhoneNumber(entity.getPhone()) : null,
                Country.fromString(entity.getCountry()),
                entity.getActiveOrganizationId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
