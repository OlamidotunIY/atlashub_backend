package com.atlashub.iam.domain.entities;

import com.atlashub.iam.domain.exception.InvalidCustomRoleException;
import com.atlashub.iam.domain.exception.InvalidRolePermissionCountException;
import com.atlashub.iam.domain.exception.RoleModificationException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CustomRoleTest {
    @Test
    void custom_role_requires_permissions() {
        assertThrows(InvalidRolePermissionCountException.class,
                () -> CustomRole.create(1L, 2L, "Manager", null, Set.of(), false, 3L));
    }

    @Test
    void owner_is_immutable_and_may_store_no_permission_ids() {
        CustomRole owner = CustomRole.create(1L, 2L, "Owner", null, Set.of(), true, 3L);
        assertThrows(RoleModificationException.class, () -> owner.addPermission(4L));
    }

    @Test
    void rejects_blank_role_name() {
        assertThrows(InvalidCustomRoleException.class,
                () -> CustomRole.create(1L, 2L, " ", null, Set.of(4L), false, 3L));
    }
}
