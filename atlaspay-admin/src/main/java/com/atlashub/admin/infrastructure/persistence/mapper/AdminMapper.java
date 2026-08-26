package com.atlashub.admin.infrastructure.persistence.mapper;

import com.atlashub.admin.domain.model.Admin;
import com.atlashub.admin.infrastructure.persistence.entity.AdminJpaEntity;
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

        AdminJpaEntity entity = new AdminJpaEntity();
        entity.setId(domain.getId());
        entity.setUsername(domain.getUsername());
        entity.setEmail(domain.getEmail().value());
        entity.setRole(domain.getRole());
        entity.setStatus(domain.getStatus());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setPermissions(domain.getPermissions() != null ? new HashSet<>(domain.getPermissions()) : new HashSet<>());
        
        return entity;
    }
}
