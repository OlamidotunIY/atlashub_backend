package com.atlashub.iam.domain.entities;

import com.atlashub.iam.domain.exception.InvalidPermissionDataException;
import com.atlashub.iam.domain.valueobject.PermissionAction;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class Permission extends AggregateRoot<Long> {
    private final Long id;
    private String code;
    private String module;
    private String resource;
    private PermissionAction action;
    private String displayName;
    private String description;
    private boolean active;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Permission(Long id, String code, String module, String resource, PermissionAction action, String displayName, String description, boolean active, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.module = module;
        this.resource = resource;
        this.action = action;
        this.displayName = displayName;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Permission create(Long id, String code, String module, String resource, PermissionAction action, String displayName, String description) {
        if (code == null || code.isBlank()) {
            throw new InvalidPermissionDataException("Permission code cannot be null or blank");
        }
        if (module == null || module.isBlank()) {
            throw new InvalidPermissionDataException("Permission module cannot be null or blank");
        }
        if (resource == null || resource.isBlank()) {
            throw new InvalidPermissionDataException("Permission resource cannot be null or blank");
        }
        if (action == null) {
            throw new InvalidPermissionDataException("Permission action cannot be null");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new InvalidPermissionDataException("Permission displayName cannot be null or blank");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidPermissionDataException("Permission description cannot be null or blank");
        }
        ZonedDateTime now = ZonedDateTime.now();
        return new Permission(id, code, module, resource, action, displayName, description, true, now, now);
    }

    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}

