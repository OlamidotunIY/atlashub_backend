package com.atlashub.admin.adapter.out.persistence.mapper;

import com.atlashub.admin.domain.model.Admin;
import com.atlashub.admin.adapter.out.persistence.entity.AdminJpaEntity;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class AdminMapper {

    public Admin toDomain(AdminJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Admin(
            entity.getId(),
            entity.getUsername(),
            new EmailAddress(entity.getEmail()),
            entity.getRole(),
            entity.getStatus(),
            entity.getCreatedBy(),
            entity.getPermissions() != null ? new HashSet<>(entity.getPermissions()) : new HashSet<>()
        );
    }

    public AdminJpaEntity toEntity(Admin domain) {
        if (domain == null) {
            return null;
        }

        return new AdminJpaEntity(
            domain.getId(),
            domain.getUsername(),
            domain.getEmail().value(),
            domain.getRole(),
            domain.getStatus(),
            domain.getCreatedBy(),
            domain.getPermissions() != null ? new HashSet<>(domain.getPermissions()) : new HashSet<>(),
            null,
            null
        );
    }
}
