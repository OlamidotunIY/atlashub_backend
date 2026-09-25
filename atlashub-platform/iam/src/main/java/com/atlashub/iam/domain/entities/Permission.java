package com.atlashub.iam.domain.entities;

import com.atlashub.iam.domain.valueobject.PermissionAction;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

@Getter
public class Permission extends AggregateRoot<Long> {
    private final Long id;
    private String code;
    private String resource;
    private PermissionAction action;
    private String displayName;
    private String description;
    private boolean isActive;

    public Permission(Long id, String code, String resource, PermissionAction action, String displayName, String description, boolean isActive) {
        this.id = id;
        this.code = code;
        this.resource = resource;
        this.action = action;
        this.displayName = displayName;
        this.description = description;
        this.isActive = isActive;
    }

    @Override
    public Long getId() {
        return id;
    }
}
