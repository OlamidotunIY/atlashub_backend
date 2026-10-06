package com.atlashub.iam.application.queries.ListPermissions;

import com.atlashub.iam.domain.repositories.PermissionRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class ListPermissionsHandlerTest {
    @Test
    void omitted_module_lists_all_active_catalog_permissions() {
        PermissionRepository repository = mock(PermissionRepository.class);
        when(repository.findAllByActiveTrue()).thenReturn(List.of());

        new ListPermissionsHandler(repository).execute(new ListPermissionsQuery(null));

        verify(repository).findAllByActiveTrue();
        verify(repository, never()).findByModule(any());
    }
}
