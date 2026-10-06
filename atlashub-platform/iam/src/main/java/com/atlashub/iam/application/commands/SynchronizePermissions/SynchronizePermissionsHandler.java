package com.atlashub.iam.application.commands.SynchronizePermissions;

import com.atlashub.iam.application.services.PlatformPermissionCatalog;
import com.atlashub.iam.domain.entities.Permission;
import com.atlashub.iam.domain.repositories.PermissionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SynchronizePermissionsHandler extends Command<SynchronizePermissionsCommand, Void> {
    private final PermissionRepository permissionRepository;

    public SynchronizePermissionsHandler(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public Void execute(SynchronizePermissionsCommand command) {
        for (PlatformPermissionCatalog.Definition definition : PlatformPermissionCatalog.definitions()) {
            if (permissionRepository.findByCode(definition.code()).isEmpty()) {
                permissionRepository.save(Permission.create(
                        permissionRepository.nextIdentity(), definition.code(), definition.module(),
                        definition.resource(), definition.action(), definition.displayName(),
                        definition.description()));
            }
        }
        return null;
    }
}
