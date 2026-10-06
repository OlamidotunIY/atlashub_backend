package com.atlashub.iam.application.commands.UpdateCustomRole;

import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.exception.InvalidPermissionDataException;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.PermissionRepository;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateCustomRoleHandlerTest {

    @Test
    void rejects_unknown_permissions_without_saving_role() {
        CustomRoleRepository roles = mock(CustomRoleRepository.class);
        PermissionRepository permissions = mock(PermissionRepository.class);
        CustomRole role = new CustomRole(3L, 10L, "Manager", "Manager", Set.of(1L), false,
                7L, ZonedDateTime.now(), ZonedDateTime.now());
        when(roles.findById(3L)).thenReturn(Optional.of(role));
        when(permissions.findAllById(Set.of(99L))).thenReturn(List.of());
        UpdateCustomRoleHandler handler = new UpdateCustomRoleHandler(roles, permissions);

        assertThrows(InvalidPermissionDataException.class, () -> handler.execute(
                new UpdateCustomRoleCommand(3L, 10L, null, null, Set.of(99L))));

        verify(roles, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
