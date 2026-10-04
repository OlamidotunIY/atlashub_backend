package com.atlashub.iam.infrastructure.configuration;

import com.atlashub.iam.application.commands.SynchronizePermissions.SynchronizePermissionsCommand;
import com.atlashub.iam.application.commands.SynchronizePermissions.SynchronizePermissionsHandler;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PermissionCatalogInitializer implements ApplicationRunner {
    private final SynchronizePermissionsHandler handler;

    public PermissionCatalogInitializer(SynchronizePermissionsHandler handler) {
        this.handler = handler;
    }

    @Override
    public void run(ApplicationArguments args) {
        handler.execute(new SynchronizePermissionsCommand());
    }
}
