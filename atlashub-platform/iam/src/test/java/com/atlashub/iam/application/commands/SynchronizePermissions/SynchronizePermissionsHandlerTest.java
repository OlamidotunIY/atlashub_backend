package com.atlashub.iam.application.commands.SynchronizePermissions;

import com.atlashub.iam.domain.entities.Permission;
import com.atlashub.iam.domain.repositories.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SynchronizePermissionsHandlerTest {
    @Test
    void creates_only_missing_catalog_permissions() {
        PermissionRepository repository = mock(PermissionRepository.class);
        when(repository.findByCode(anyString())).thenReturn(Optional.empty());
        when(repository.nextIdentity()).thenReturn(1L);
        SynchronizePermissionsHandler handler = new SynchronizePermissionsHandler(repository);

        handler.execute(new SynchronizePermissionsCommand());

        ArgumentCaptor<Permission> saved = ArgumentCaptor.forClass(Permission.class);
        verify(repository, atLeastOnce()).save(saved.capture());
        assertFalse(saved.getAllValues().isEmpty());
    }
}
