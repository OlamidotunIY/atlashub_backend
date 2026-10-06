package com.atlashub.iam.application.commands.CreateCustomRole;

import com.atlashub.iam.domain.entities.Permission;
import com.atlashub.iam.domain.exception.InvalidPermissionDataException;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.PermissionRepository;
import com.atlashub.iam.domain.valueobject.PermissionAction;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateCustomRoleHandlerTest {

    @Test
    void rejects_missing_or_inactive_permissions_before_creating_role() {
        CustomRoleRepository roles = mock(CustomRoleRepository.class);
        PermissionRepository permissions = mock(PermissionRepository.class);
        Permission inactive = new Permission(1L, "iam:members:read", "iam", "members",
                PermissionAction.READ, "Read members", "Read members", false,
                ZonedDateTime.now(), ZonedDateTime.now());
        when(permissions.findAllById(Set.of(1L, 2L))).thenReturn(List.of(inactive));
        CreateCustomRoleHandler handler = new CreateCustomRoleHandler(roles, permissions);

        assertThrows(InvalidPermissionDataException.class, () -> handler.execute(
                new CreateCustomRoleCommand(10L, "Manager", "Manager role", Set.of(1L, 2L), 7L)));

        verify(roles, never()).nextIdentity();
        verify(roles, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
