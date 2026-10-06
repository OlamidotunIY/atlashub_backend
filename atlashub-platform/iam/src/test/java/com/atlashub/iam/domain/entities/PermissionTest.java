package com.atlashub.iam.domain.entities;

import com.atlashub.iam.domain.exception.InvalidPermissionDataException;
import com.atlashub.iam.domain.valueobject.PermissionAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermissionTest {
    @Test
    void creates_active_permission_with_valid_code() {
        Permission permission = Permission.create(1L, "iam:roles:manage", "iam", "roles",
                PermissionAction.MANAGE, "Manage roles", "Manage organization roles");
        assertTrue(permission.isActive());
    }

    @Test
    void creates_active_permission_with_module_action_code() {
        Permission permission = Permission.create(1L, "compliance:read", "compliance", "compliance",
                PermissionAction.READ, "View compliance status", "View compliance status");

        assertTrue(permission.isActive());
    }

    @Test
    void rejects_invalid_code_format() {
        assertThrows(InvalidPermissionDataException.class,
                () -> Permission.create(1L, "bad-code", "iam", "roles",
                        PermissionAction.MANAGE, "Manage roles", "Manage organization roles"));
    }
}
