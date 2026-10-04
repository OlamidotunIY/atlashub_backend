package com.atlashub.iam.infrastructure.configuration;

import com.atlashub.iam.application.commands.SynchronizePermissions.SynchronizePermissionsCommand;
import com.atlashub.iam.application.commands.SynchronizePermissions.SynchronizePermissionsHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PermissionCatalogInitializerTest {
    @Test
    void delegates_startup_to_synchronization_command() {
        SynchronizePermissionsHandler handler = mock(SynchronizePermissionsHandler.class);
        new PermissionCatalogInitializer(handler).run(new DefaultApplicationArguments(new String[0]));
        verify(handler).execute(new SynchronizePermissionsCommand());
    }
}
