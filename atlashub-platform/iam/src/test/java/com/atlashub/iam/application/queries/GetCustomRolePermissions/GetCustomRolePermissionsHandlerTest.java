package com.atlashub.iam.application.queries.GetCustomRolePermissions;

import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.entities.Permission;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.PermissionRepository;
import com.atlashub.iam.domain.valueobject.PermissionAction;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetCustomRolePermissionsHandlerTest {

    @Test
    void built_in_owner_role_resolves_all_current_active_permissions() {
        CustomRoleRepository roles = mock(CustomRoleRepository.class);
        PermissionRepository permissions = mock(PermissionRepository.class);
        CustomRole owner = new CustomRole(3L, 10L, "Owner", "Owner", Set.of(), true,
                7L, ZonedDateTime.now(), ZonedDateTime.now());
        Permission active = new Permission(5L, "iam:members:read", "iam", "members",
                PermissionAction.READ, "Read members", "Read members", true,
                ZonedDateTime.now(), ZonedDateTime.now());
        when(roles.findById(3L)).thenReturn(Optional.of(owner));
        when(permissions.findAllByActiveTrue()).thenReturn(List.of(active));
        GetCustomRolePermissionsHandler handler = new GetCustomRolePermissionsHandler(roles, permissions);

        var result = handler.execute(new GetCustomRolePermissionsQuery(3L, 10L));

        assertEquals(List.of("iam:members:read"),
                result.permissions().stream().map(CustomRolePermissionsResult.PermissionDetail::code).toList());
        verify(permissions).findAllByActiveTrue();
    }
}
